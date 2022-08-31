package cn.mine.community.controller;

import cn.mine.community.entity.DiscussPost;
import cn.mine.community.service.DiscussPostService;
import cn.mine.community.service.LikeService;
import cn.mine.community.service.UserService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.Page;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String getIndexPage() {
        return "redirect:/index";
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getIndexPage(Page<DiscussPost> page, Model model, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        PageInfo<DiscussPost> pageInfo = discussPostService.selectDiscussPostsByPage(0, page.getPageNum(), page.getPageSize(), orderMode);

        List<DiscussPost> list = pageInfo.getList();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                Map<String, Object> attributes = discussPost.getAttributes();
                attributes.put("user", userService.findUserById(discussPost.getUserId()));
                attributes.put("likeCount", likeService.findEntityLikeCount(ConstantUtil.ENTITY_TYPE_POST, discussPost.getId()));
            }
        }

        page.setList(list);
        page.setTotal(pageInfo.getTotal());
        page.setPath("/index?orderMode=" + orderMode);
        model.addAttribute("orderMode", orderMode);

        return "index";
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "error/500";
    }

    @RequestMapping(value = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "error/404";
    }
}
