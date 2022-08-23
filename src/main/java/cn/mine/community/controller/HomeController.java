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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
    public String getIndexPage(Page<DiscussPost> page) {
        PageInfo<DiscussPost> pageInfo = discussPostService.selectDiscussPostsByPage(0, page.getPageNum(), page.getPageSize());

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
        page.setPath("/index");

        return "index";
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "error/500";
    }
}
