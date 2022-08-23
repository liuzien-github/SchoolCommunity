package cn.mine.community.service.impl;

import cn.mine.community.service.LikeService;
import cn.mine.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                Boolean flag = operations.opsForSet().isMember(key, userId);

                operations.multi();
                if (flag) {
                    operations.opsForSet().remove(key, userId);
                    operations.opsForValue().decrement(userKey);
                } else {
                    operations.opsForSet().add(key, userId);
                    operations.opsForValue().increment(userKey);
                }

                return operations.exec();
            }
        });
    }

    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public int isEntityLike(int userId, int entityType, int entityId) {
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        if (redisTemplate.opsForSet().isMember(key, userId))
            return 1;
        else
            return 0;
    }

    @Override
    public int findUserLikeCount(int userId) {
        String userKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userKey);
        if (count == null)
            return 0;
        else
            return count;
    }
}
