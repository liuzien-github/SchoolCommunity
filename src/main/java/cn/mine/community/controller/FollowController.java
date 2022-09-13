package cn.mine.community.controller;

import cn.mine.community.annotation.LoginCheck;
import cn.mine.community.entity.Event;
import cn.mine.community.entity.User;
import cn.mine.community.event.EventProducer;
import cn.mine.community.service.FollowService;
import cn.mine.community.service.UserService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.GeneralUtil;
import cn.mine.community.util.HostHolder;
import cn.mine.community.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowController {
    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @LoginCheck
    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        followService.follow(HostHolder.getUser().getId(), entityType, entityId);

        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_FOLLOW)
                .setUserId(HostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return GeneralUtil.getJsonString(0, "关注成功！");
    }

    @LoginCheck
    @RequestMapping(value = "/unFollow", method = RequestMethod.POST)
    @ResponseBody
    public String unFollow(int entityType, int entityId) {
        followService.unFollow(HostHolder.getUser().getId(), entityType, entityId);
        return GeneralUtil.getJsonString(0, "已取消关注！");
    }

    @LoginCheck
    @RequestMapping(value = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page<User> page, Model model) {
        Page<User> tempPage = followService.findFollowees(userId, page.getPageNum(), page.getPageSize());
        if (tempPage == null)
            throw new RuntimeException("该用户不存在！");

        page.setList(tempPage.getList());
        page.setTotal(tempPage.getTotal());
        page.setPath("/followees/" + userId);
        model.addAttribute("user", userService.findUserById(userId));

        return "site/followee";
    }

    @LoginCheck
    @RequestMapping(value = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page<User> page, Model model) {
        Page<User> tempPage = followService.findFollowers(userId, page.getPageNum(), page.getPageSize());
        if (tempPage == null)
            throw new RuntimeException("该用户不存在！");

        page.setList(tempPage.getList());
        page.setTotal(tempPage.getTotal());
        page.setPath("/followers/" + userId);
        model.addAttribute("user", userService.findUserById(userId));

        return "site/follower";
    }
}
