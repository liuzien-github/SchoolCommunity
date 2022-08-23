package cn.mine.community.controller;

import cn.mine.community.annotation.LoginCheck;
import cn.mine.community.entity.Message;
import cn.mine.community.entity.User;
import cn.mine.community.service.MessageService;
import cn.mine.community.service.UserService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.GeneralUtil;
import cn.mine.community.util.HostHolder;
import cn.mine.community.util.Page;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.validation.constraints.NotNull;
import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @LoginCheck
    @RequestMapping(value = "/letter/list", method = RequestMethod.GET)
    public String getLettersList(Model model, Page<Message> page) {
        User user = HostHolder.getUser();
        PageInfo<Message> pageInfo = messageService.selectConversationsByPage(user.getId(), page.getPageNum(), page.getPageSize());

        List<Message> list = pageInfo.getList();
        if (list != null) {
            for (Message message : list) {
                Map<String, Object> attributes = message.getAttributes();
                attributes.put("lettersCount", messageService.selectConversationLettersCount(message.getConversationId()));
                attributes.put("unReadLettersCount", messageService.selectUnreadLettersCount(user.getId(), message.getConversationId()));
                if (user.getId() == message.getFromId())
                    attributes.put("targetUser", userService.findUserById(message.getToId()));
                else
                    attributes.put("targetUser", userService.findUserById(message.getFromId()));
            }
        }

        page.setList(list);
        page.setTotal(pageInfo.getTotal());
        page.setPath("/letter/list");
        model.addAttribute("allUnReadLettersCount", messageService.selectUnreadLettersCount(user.getId(), null));
        model.addAttribute("allUnReadNoticesCount", messageService.selectUnreadNoticesCount(user.getId(), null));

        return "site/letter";
    }

    @LoginCheck
    @RequestMapping(value = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(Model model, @PathVariable("conversationId") String conversationId, Page<Message> page) {
        PageInfo<Message> pageInfo = messageService.selectLettersByPage(conversationId, page.getPageNum(), page.getPageSize());
        page.setList(pageInfo.getList());
        page.setTotal(pageInfo.getTotal());
        page.setPath("/letter/detail/" + conversationId);

        User user = HostHolder.getUser();
        Message message = pageInfo.getList().get(0);
        if (user.getId() == message.getFromId()) {
            model.addAttribute("targetUser", userService.findUserById(message.getToId()));
        } else {
            model.addAttribute("targetUser", userService.findUserById(message.getFromId()));
        }

        List<Integer> ids = getUnReadMessage(pageInfo);
        if (!ids.isEmpty())
            messageService.updateReadStatus(ids);

        return "site/letter-detail";
    }

    @LoginCheck
    @RequestMapping(value = "/letter/add", method = RequestMethod.POST)
    @ResponseBody
    public String addLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return GeneralUtil.getJsonString(1, "目标用户不存在！");
        }

        Message message = new Message()
                .setFromId(HostHolder.getUser().getId())
                .setToId(target.getId())
                .setContent(content)
                .setStatus(0)
                .setCreateTime(new Date());
        if (message.getFromId() <= message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }

        messageService.addLetter(message);
        return GeneralUtil.getJsonString(0);
    }

    @LoginCheck
    @RequestMapping(value = "/letter/delete/{conversationId}/{pageNum}/{size}/{id}", method = RequestMethod.GET)
    public String deleteMessage(@PathVariable("conversationId") String conversationId, @PathVariable("pageNum") Integer pageNum, @PathVariable("id") Integer id, @PathVariable("size") Integer size) {
        messageService.deleteLetter(id);
        if (size > 1)
            return "redirect:/letter/detail/" + conversationId + "(pageNum=" + pageNum + ")";
        else if (pageNum > 1)
            return "redirect:/letter/detail/" + conversationId + "(pageNum=" + (pageNum - 1) + ")";
        else
            return "redirect:/letter/list";
    }

    @LoginCheck
    @RequestMapping(value = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = HostHolder.getUser();
        Map<String, Object> messageMap = null;
        Message message = null;

        message = messageService.selectLatestNotice(user.getId(), ConstantUtil.TOPIC_COMMENT);
        messageMap = new HashMap<>();
        if (message != null) {
            messageMap.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageMap.put("user", userService.findUserById((Integer) data.get("userId")));
            messageMap.put("entityType", data.get("entityType"));
            messageMap.put("entityId", data.get("entityId"));
            messageMap.put("discussPostId", data.get("discussPostId"));

            messageMap.put("count", messageService.selectNoticeCount(user.getId(), ConstantUtil.TOPIC_COMMENT));
            messageMap.put("unreadCount", messageService.selectUnreadNoticesCount(user.getId(), ConstantUtil.TOPIC_COMMENT));
        }
        model.addAttribute("messageComment", messageMap);

        message = messageService.selectLatestNotice(user.getId(), ConstantUtil.TOPIC_FOLLOW);
        messageMap = new HashMap<>();
        if (message != null) {
            messageMap.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageMap.put("user", userService.findUserById((Integer) data.get("userId")));
            messageMap.put("entityType", data.get("entityType"));
            messageMap.put("entityId", data.get("entityId"));

            messageMap.put("count", messageService.selectNoticeCount(user.getId(), ConstantUtil.TOPIC_FOLLOW));
            messageMap.put("unreadCount", messageService.selectUnreadNoticesCount(user.getId(), ConstantUtil.TOPIC_FOLLOW));
        }
        model.addAttribute("messageFollow", messageMap);

        message = messageService.selectLatestNotice(user.getId(), ConstantUtil.TOPIC_LIKE);
        messageMap = new HashMap<>();
        if (message != null) {
            messageMap.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageMap.put("user", userService.findUserById((Integer) data.get("userId")));
            messageMap.put("entityType", data.get("entityType"));
            messageMap.put("entityId", data.get("entityId"));
            messageMap.put("discussPostId", data.get("discussPostId"));

            messageMap.put("count", messageService.selectNoticeCount(user.getId(), ConstantUtil.TOPIC_LIKE));
            messageMap.put("unreadCount", messageService.selectUnreadNoticesCount(user.getId(), ConstantUtil.TOPIC_LIKE));
        }
        model.addAttribute("messageLike", messageMap);

        model.addAttribute("allUnReadNoticesCount", messageService.selectUnreadNoticesCount(user.getId(), null));
        model.addAttribute("allUnReadLettersCount", messageService.selectUnreadLettersCount(user.getId(), null));

        return "site/notice";
    }

    @LoginCheck
    @RequestMapping(value = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(Model model, @PathVariable("topic") String topic, Page<Message> page) {
        User user = HostHolder.getUser();
        PageInfo<Message> pageInfo = messageService.selectNoticesByPage(user.getId(), topic, page.getPageNum(), page.getPageSize());

        List<Message> list = pageInfo.getList();
        if (list != null) {
            for (Message notice: list) {
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                Map<String, Object> attributes = notice.getAttributes();
                attributes.put("user", userService.findUserById((Integer) data.get("userId")));
                attributes.put("entityType", data.get("entityType"));
                attributes.put("entityId", data.get("entityId"));
                attributes.put("discussPostId", data.get("discussPostId"));
                attributes.put("fromUser", userService.findUserById(notice.getFromId()));
            }
        }

        page.setList(pageInfo.getList());
        page.setTotal(pageInfo.getTotal());
        page.setPath("/notice/detail/" + topic);

        List<Integer> ids = getUnReadMessage(pageInfo);
        if (!ids.isEmpty())
            messageService.updateReadStatus(ids);

        return "site/notice-detail";
    }

    private List<Integer> getUnReadMessage(@NotNull PageInfo<Message> pageInfo) {
        List<Integer> ids = new ArrayList<>();
        List<Message> list = pageInfo.getList();
        if (list != null) {
            for (Message message : list) {
                if (message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }
}
