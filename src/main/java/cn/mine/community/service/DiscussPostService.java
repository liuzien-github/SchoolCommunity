package cn.mine.community.service;

import cn.mine.community.entity.DiscussPost;
import com.github.pagehelper.PageInfo;

public interface DiscussPostService {
    PageInfo<DiscussPost> selectDiscussPostsByPage(int userId, int pageNum, int pageSize, int orderMode);
    int displayDiscussPost(DiscussPost discussPost);
    DiscussPost findDiscussPostById(int discussPostId);
    int updateDiscussPostTypeById(int discussPostId, int type);
    int updateDiscussPostStatusById(int discussPostId, int status);
    int deleteDiscussPostById(int discussPostId);
    int updateDiscussPostScoreById(int discussPostId, double score);
}
