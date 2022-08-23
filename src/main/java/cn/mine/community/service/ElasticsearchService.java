package cn.mine.community.service;

import cn.mine.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface ElasticsearchService {
    void saveDiscussPost(DiscussPost discussPost);
    void deleteDiscussPostById(int discussPostId);
    Page<DiscussPost> searchDiscussPostsByPage(String keyWord, int pageNum, int pageSize);
}
