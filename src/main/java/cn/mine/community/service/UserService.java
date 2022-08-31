package cn.mine.community.service;

import cn.mine.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public interface UserService {
    User findUserById(int userId);
    User findUserByName(String username);
    int updateUserById(User user);
    Map<String, Object> register(User user);
    void unRegister(int userId);
    int activate(int userId, String activationCode);
    Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
