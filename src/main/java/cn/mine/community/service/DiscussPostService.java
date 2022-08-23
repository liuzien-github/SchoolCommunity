package cn.mine.community.service;

import cn.mine.community.entity.DiscussPost;
import com.github.pagehelper.PageInfo;

public interface DiscussPostService {
    PageInfo<DiscussPost> selectDiscussPostsByPage(int userId, int pageNum, int pageSize);
    int displayDiscussPost(DiscussPost discussPost);
    DiscussPost findDiscussPostById(int discussPostId);
    void deleteDiscussPostById(int discussPostId);
}
