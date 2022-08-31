package cn.mine.community.controller;

import cn.mine.community.entity.Comment;
import cn.mine.community.entity.DiscussPost;
import cn.mine.community.entity.Event;
import cn.mine.community.entity.User;
import cn.mine.community.event.EventProducer;
import cn.mine.community.service.CommentService;
import cn.mine.community.service.DiscussPostService;
import cn.mine.community.service.LikeService;
import cn.mine.community.service.UserService;
import cn.mine.community.util.*;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/display", method = RequestMethod.POST)
    @ResponseBody
    public String displayDiscussPost(String title, String content) {
        User user = HostHolder.getUser();
        if (user == null) {
            return GeneralUtil.getJsonString(403, "您还没有登录哦！");
        }

        DiscussPost discussPost = new DiscussPost()
                .setUserId(user.getId())
                .setTitle(title)
                .setContent(content)
                .setCreateTime(new Date());
        discussPostService.displayDiscussPost(discussPost);

        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ConstantUtil.ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        redisTemplate.opsForSet().add(RedisKeyUtil.getPostScoreKey(), discussPost.getId());

        return GeneralUtil.getJsonString(0, "帖子发表成功！");
    }

    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDetailDiscussPost(@PathVariable("discussPostId") Integer discussPostId, Page<Comment> page, Model model) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", discussPost);

        Map<String, Object> attributes = discussPost.getAttributes();
        attributes.put("user", userService.findUserById(discussPost.getUserId()));
        attributes.put("likeCount", likeService.findEntityLikeCount(ConstantUtil.ENTITY_TYPE_POST, discussPost.getId()));

        User user = HostHolder.getUser();
        if (user != null)
            attributes.put("likeStatus", likeService.isEntityLike(user.getId(), ConstantUtil.ENTITY_TYPE_POST, discussPost.getId()));
        else
            attributes.put("likeStatus", 0);

        PageInfo<Comment> pageInfo = commentService.selectCommentsByPage(ConstantUtil.ENTITY_TYPE_POST, discussPostId, page.getPageNum(), page.getPageSize());
        List<Comment> comments = pageInfo.getList();
        if (comments != null) {
            for (Comment comment : comments) {
                PageInfo<Comment> replys = commentService.selectCommentsByPage(ConstantUtil.ENTITY_TYPE_COMMENT, comment.getId(), 1, Integer.MAX_VALUE);
                Map<String, Object> attributesComment = comment.getAttributes();
                attributesComment.put("replysList", replys.getList());
                attributesComment.put("replysListSize", replys.getList().size());
                attributesComment.put("likeCount", likeService.findEntityLikeCount(ConstantUtil.ENTITY_TYPE_COMMENT, comment.getId()));
                attributesComment.put("user", userService.findUserById(comment.getUserId()));
                if (user != null)
                    attributesComment.put("likeStatus", likeService.isEntityLike(user.getId(), ConstantUtil.ENTITY_TYPE_COMMENT, comment.getId()));
                else
                    attributesComment.put("likeStatus", 0);

                List<Comment> replysList = replys.getList();
                if (replysList != null) {
                    for (Comment reply : replysList) {
                        Map<String, Object> attributesReply = reply.getAttributes();
                        attributesReply.put("target", userService.findUserById(reply.getTargetId()));
                        attributesReply.put("likeCount", likeService.findEntityLikeCount(ConstantUtil.ENTITY_TYPE_COMMENT, reply.getId()));
                        attributesReply.put("user", userService.findUserById(reply.getUserId()));
                        if (user != null)
                            attributesReply.put("likeStatus", likeService.isEntityLike(user.getId(), ConstantUtil.ENTITY_TYPE_COMMENT, reply.getId()));
                        else
                            attributesReply.put("likeStatus", 0);
                    }
                }
            }
        }

        page.setList(comments);
        page.setTotal(pageInfo.getTotal());
        page.setPath("/discuss/detail/" + discussPostId);

        return "site/discuss-detail";
    }

    @RequestMapping(value = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int discussPostId) {
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        int newType = 0;
        if (post.getType() == 0)
            newType = 1;
        discussPostService.updateDiscussPostTypeById(discussPostId, newType);

        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_PUBLISH)
                .setUserId(HostHolder.getUser().getId())
                .setEntityType(ConstantUtil.ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
        eventProducer.fireEvent(event);

        return GeneralUtil.getJsonString(0);
    }

    @RequestMapping(value = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int discussPostId) {
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        int newStatus = 0;
        if (post.getStatus() == 0)
            newStatus = 1;
        discussPostService.updateDiscussPostStatusById(discussPostId, newStatus);

        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_PUBLISH)
                .setUserId(HostHolder.getUser().getId())
                .setEntityType(ConstantUtil.ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
        eventProducer.fireEvent(event);

        redisTemplate.opsForSet().add(RedisKeyUtil.getPostScoreKey(), post.getId());

        return GeneralUtil.getJsonString(0);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int discussPostId) {
        discussPostService.deleteDiscussPostById(discussPostId);

        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_DELETE)
                .setUserId(HostHolder.getUser().getId())
                .setEntityType(ConstantUtil.ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
        eventProducer.fireEvent(event);

        return GeneralUtil.getJsonString(0);
    }
}
