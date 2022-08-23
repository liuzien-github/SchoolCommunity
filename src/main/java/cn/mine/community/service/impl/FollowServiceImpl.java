package cn.mine.community.service.impl;

import cn.mine.community.entity.User;
import cn.mine.community.service.FollowService;
import cn.mine.community.service.UserService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.HostHolder;
import cn.mine.community.util.Page;
import cn.mine.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;

    @Override
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    @Override
    public void unFollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    @Override
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().size(followeeKey);
    }

    @Override
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().size(followerKey);
    }

    @Override
    public boolean isFollow(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    @Override
    public Page<User> findFollowees(int userId, int pageNum, int pageSize) {
        String key = RedisKeyUtil.getFolloweeKey(userId, ConstantUtil.ENTITY_TYPE_USER);
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize - 1;
        long total = redisTemplate.opsForZSet().size(key);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(key, start, end);
        if (targetIds == null)
            return null;

        List<User> list = new ArrayList<>();
        for (Object i : targetIds) {
            Integer id = (Integer) i;
            User user = userService.findUserById(id);
            list.add(user);
            Double score = redisTemplate.opsForZSet().score(key, id);
            user.getAttributes().put("followTime", new Date(score.longValue()));
            user.getAttributes().put("isFollow", isFollow(HostHolder.getUser().getId(), ConstantUtil.ENTITY_TYPE_USER, id));
        }

        Page<User> page = new Page<>();
        page.setList(list);
        page.setTotal(total);
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);

        return page;
    }

    @Override
    public Page<User> findFollowers(int userId, int pageNum, int pageSize) {
        String key = RedisKeyUtil.getFollowerKey(ConstantUtil.ENTITY_TYPE_USER, userId);
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize - 1;
        long total = redisTemplate.opsForZSet().size(key);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(key, start, end);
        if (targetIds == null)
            return null;

        List<User> list = new ArrayList<>();
        for (Object i : targetIds) {
            Integer id = (Integer) i;
            User user = userService.findUserById(id);
            list.add(user);
            Double score = redisTemplate.opsForZSet().score(key, id);
            user.getAttributes().put("followTime", new Date(score.longValue()));
            user.getAttributes().put("isFollow", isFollow(HostHolder.getUser().getId(), ConstantUtil.ENTITY_TYPE_USER, id));
        }

        Page<User> page = new Page<>();
        page.setList(list);
        page.setTotal(total);
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);

        return page;
    }
}
