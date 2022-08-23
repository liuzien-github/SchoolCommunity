package cn.mine.community.service.impl;

import cn.mine.community.dao.CommentMapper;
import cn.mine.community.dao.DiscussPostMapper;
import cn.mine.community.entity.Comment;
import cn.mine.community.entity.DiscussPost;
import cn.mine.community.service.CommentService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.SensitiveWordsFilter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveWordsFilter filter;

    @Override
    public PageInfo<Comment> selectCommentsByPage(int entityType, int entityId, int pageNum, int pageSize) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("entity_type", entityType)
                .eq("entity_id", entityId)
                .orderByDesc("id");

        PageHelper.startPage(pageNum, pageSize);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        return new PageInfo<>(comments);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(filter.filter(comment.getContent()));
        int rows = commentMapper.insert(comment);

        if (comment.getEntityType() == ConstantUtil.ENTITY_TYPE_POST) {
            int newCommentCount = discussPostMapper.selectById(comment.getEntityId()).getCommentCount() + 1;
            discussPostMapper.updateById(new DiscussPost().setId(comment.getEntityId()).setCommentCount(newCommentCount));
        }

        return rows;
    }

    @Override
    public Comment findCommentById(int commentId) {
        return commentMapper.selectById(commentId);
    }

    @Override
    public void deleteCommentById(int commentId) {
        commentMapper.deleteById(commentId);
    }
}
