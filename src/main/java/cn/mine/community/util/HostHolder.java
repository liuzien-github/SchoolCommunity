package cn.mine.community.util;

import cn.mine.community.entity.User;

public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public static void setUser(User user) {
        users.set(user);
    }

    public static User getUser() {
        return users.get();
    }

    public static void clear() {
        users.remove();
    }
}
