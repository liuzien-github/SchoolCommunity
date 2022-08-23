package cn.mine.community.controller;

import cn.mine.community.annotation.LoginCheck;
import cn.mine.community.entity.Comment;
import cn.mine.community.entity.DiscussPost;
import cn.mine.community.entity.Event;
import cn.mine.community.event.EventProducer;
import cn.mine.community.service.CommentService;
import cn.mine.community.service.DiscussPostService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @LoginCheck
    @RequestMapping(value = "/comment/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") Integer discussPostId, Comment comment) {
        comment.setUserId(HostHolder.getUser().getId()).setStatus(0).setCreateTime(new Date());
        commentService.addComment(comment);

        Event event = null;
        event = new Event()
                .setTopic(ConstantUtil.TOPIC_COMMENT)
                .setUserId(HostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId());
        event.getAttributes().put("discussPostId", discussPostId);
        if (comment.getEntityType() == ConstantUtil.ENTITY_TYPE_POST) {
            DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(discussPost.getUserId());
        } else if (comment.getEntityType() == ConstantUtil.ENTITY_TYPE_COMMENT) {
            Comment commentById = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(commentById.getUserId());
        }
        eventProducer.fireEvent(event);

        //如果评论的是帖子，那么帖子的评论数字段将发生变化，es中的内容需要重新更新
        if (comment.getEntityType() == ConstantUtil.ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(ConstantUtil.TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ConstantUtil.ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
