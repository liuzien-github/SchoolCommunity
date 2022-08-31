package cn.mine.community.service.impl;

import cn.mine.community.dao.UserMapper;
import cn.mine.community.entity.User;
import cn.mine.community.service.UserService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.GeneralUtil;
import cn.mine.community.util.MailClient;
import cn.mine.community.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User u = userMapper.selectOne(queryWrapper);

        if (u != null) {
            map.put("usernameMsg", "账号已经存在！");
            return map;
        }

        queryWrapper.clear();
        queryWrapper.eq("email", user.getEmail());
        u = userMapper.selectOne(queryWrapper);

        if (u != null) {
            map.put("emailMsg", "邮箱已经注册！");
            return map;
        }

        user.setSalt(GeneralUtil.generateUUID().substring(0, 5));
        user.setPassword(GeneralUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(GeneralUtil.generateUUID());
        user.setCreateTime(new Date());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));

        userMapper.insert(user);

        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    @Override
    public void unRegister(int userId) {
        userMapper.deleteById(userId);
    }

    @Override
    public int activate(int userId, String activationCode) {
        User user = findUserById(userId);

        if (user == null)
            return ConstantUtil.ACTIVATION_UNREGISTER;

        if (user.getStatus() == 1)
            return ConstantUtil.ACTIVATION_REPEAT;

        if (user.getActivationCode().equals(activationCode)) {
            updateUserById(new User().setId(userId).setStatus(1));
            return ConstantUtil.ACTIVATION_SUCCESS;
        }

        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return ConstantUtil.ACTIVATION_FAILURE;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return ConstantUtil.AUTHORITY_ADMIN;
                    case 2:
                        return ConstantUtil.AUTHORITY_MODERATOR;
                    default:
                        return ConstantUtil.AUTHORITY_USER;
                }
            }
        });
        return list;
    }

    @Override
    public User findUserById(int userId) {
        User user = getUserFromCache(userId);
        if (user == null)
            user = initUsertoCache(userId);
        return user;
    }

    @Override
    public User findUserByName(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public int updateUserById(User user) {
        clearUserFromCache(user.getId());
        return userMapper.updateById(user);
    }

    private User getUserFromCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    private User initUsertoCache(int userId) {
        User user = userMapper.selectById(userId);

        if (user != null) {
            String userKey = RedisKeyUtil.getUserKey(userId);
            redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        }

        return user;
    }

    private void clearUserFromCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
