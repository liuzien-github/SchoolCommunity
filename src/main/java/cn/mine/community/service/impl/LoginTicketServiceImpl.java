package cn.mine.community.service.impl;

import cn.mine.community.dao.UserMapper;
import cn.mine.community.entity.LoginTicket;
import cn.mine.community.entity.User;
import cn.mine.community.service.LoginTicketService;
import cn.mine.community.util.GeneralUtil;
import cn.mine.community.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LoginTicketServiceImpl implements LoginTicketService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        password = GeneralUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误！");
            return map;
        }

        Date date = new Date(System.currentTimeMillis() + expiredSeconds * 1000);
        LoginTicket loginTicket = new LoginTicket()
                .setUserId(user.getId())
                .setTicket(GeneralUtil.generateUUID())
                .setStatus(0)
                .setExpired(date);

        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        //redisTemplate可以帮助将实体对象序列化，但是实体类必须实现Serializable接口
        redisTemplate.opsForValue().set(ticketKey, loginTicket, expiredSeconds, TimeUnit.SECONDS);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    @Override
    public void logout(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        redisTemplate.delete(ticketKey);
    }

    @Override
    public LoginTicket getLoginTicketByTicket(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        //redisTemplate可以帮助将实体对象反序列化，但是实体类必须实现Serializable接口
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }
}
