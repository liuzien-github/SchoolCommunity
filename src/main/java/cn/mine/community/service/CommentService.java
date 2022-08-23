package cn.mine.community.service;

import cn.mine.community.entity.Comment;
import com.github.pagehelper.PageInfo;

public interface CommentService {
    PageInfo<Comment> selectCommentsByPage(int entityType, int entityId, int pageNum, int pageSize);
    int addComment(Comment comment);
    Comment findCommentById(int commentId);
    void deleteCommentById(int commentId);
}
