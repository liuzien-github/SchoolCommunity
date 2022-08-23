package cn.mine.community.service;

import cn.mine.community.entity.User;
import cn.mine.community.util.Page;

public interface FollowService {
    void follow(int userId, int entityType, int entityId);
    void unFollow(int userId, int entityType, int entityId);
    long findFolloweeCount(int userId, int entityType);
    long findFollowerCount(int entityType, int entityId);
    boolean isFollow(int userId, int entityType, int entityId);
    Page<User> findFollowees(int userId, int pageNum, int pageSize);
    Page<User> findFollowers(int userId, int pageNum, int pageSize);
}
