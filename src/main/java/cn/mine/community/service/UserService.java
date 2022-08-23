package cn.mine.community.service;

import cn.mine.community.entity.User;

import java.util.Map;

public interface UserService {
    User findUserById(int userId);
    User findUserByName(String username);
    int updateUserById(User user);
    Map<String, Object> register(User user);
    void unRegister(int userId);
    int activate(int userId, String activationCode);
}
