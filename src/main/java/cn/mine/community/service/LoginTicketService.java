package cn.mine.community.service;

import cn.mine.community.entity.LoginTicket;

import java.util.Map;

public interface LoginTicketService {
    Map<String, Object> login(String username, String password, long expiredSeconds);
    void logout(String ticket);
    LoginTicket getLoginTicketByTicket(String ticket);
}
