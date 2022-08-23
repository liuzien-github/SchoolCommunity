package cn.mine.community.controller;

import cn.mine.community.entity.DiscussPost;
import cn.mine.community.service.ElasticsearchService;
import cn.mine.community.service.LikeService;
import cn.mine.community.service.UserService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page<DiscussPost> page, Model model) {
        org.springframework.data.domain.Page<DiscussPost> p = elasticsearchService.searchDiscussPostsByPage(keyword, page.getPageNum(), page.getPageSize());

        List<DiscussPost> list = new ArrayList<>();
        if (p != null) {
            for (DiscussPost discussPost : p) {
                Map<String, Object> attributes = discussPost.getAttributes();
                attributes.put("user", userService.findUserById(discussPost.getUserId()));
                attributes.put("likeCount", likeService.findEntityLikeCount(ConstantUtil.ENTITY_TYPE_POST, discussPost.getId()));
                list.add(discussPost);
            }
        }

        page.setList(list);
        page.setTotal(p == null ? 0 : p.getTotalElements());
        page.setPath("/search?keyword=" + keyword);
        model.addAttribute("keyword", keyword);

        return "site/search";
    }
}
