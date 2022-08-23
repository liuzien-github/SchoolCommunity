package cn.mine.community.service.impl;

import cn.mine.community.dao.DiscussPostMapper;
import cn.mine.community.entity.DiscussPost;
import cn.mine.community.service.DiscussPostService;
import cn.mine.community.util.SensitiveWordsFilter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveWordsFilter filter;

    @Override
    public PageInfo<DiscussPost> selectDiscussPostsByPage(int userId, int pageNum, int pageSize) {
        QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (userId > 0)
            queryWrapper.eq("user_id", userId);

        PageHelper.startPage(pageNum, pageSize);
        List<DiscussPost> discussPosts = discussPostMapper.selectList(queryWrapper);
        return new PageInfo<>(discussPosts);
    }

    @Override
    public int displayDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        discussPost.setTitle(filter.filter(discussPost.getTitle()));
        discussPost.setContent(filter.filter(discussPost.getContent()));

        return discussPostMapper.insert(discussPost);
    }

    @Override
    public DiscussPost findDiscussPostById(int discussPostId) {
        return discussPostMapper.selectById(discussPostId);
    }

    @Override
    public void deleteDiscussPostById(int discussPostId) {
        discussPostMapper.deleteById(discussPostId);
    }
}
