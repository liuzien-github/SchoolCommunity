package cn.mine.community.controller;

import cn.mine.community.annotation.LoginCheck;
import cn.mine.community.entity.User;
import cn.mine.community.service.FollowService;
import cn.mine.community.service.LikeService;
import cn.mine.community.service.UserService;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.GeneralUtil;
import cn.mine.community.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

@Slf4j
@Controller
public class UserController {
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.upload-path}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginCheck
    @RequestMapping(value = "/user/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }

    @LoginCheck
    @RequestMapping(value = "/user/upload", method = RequestMethod.POST)
    public String upload(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "site/setting";
        } else {
            String originalFilename = headerImage.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!".png".equals(suffix) && !".jpg".equals(suffix) && !".jpeg".equals(suffix)) {
                model.addAttribute("error", "图片格式错误，应为.png或.jpg或.jpeg格式！");
                return "site/setting";
            }
            String newFilename = GeneralUtil.generateUUID() + suffix;
            File dest = new File(uploadPath + newFilename);
            try {
                headerImage.transferTo(dest);
            } catch (IOException e) {
                log.error("文件上传失败：" + e.getMessage());
                throw new RuntimeException("上传文件失败，服务器发生异常！", e);
            }
            String newHeadUrl = domain + contextPath + "/user/header/" + newFilename;
            User user = HostHolder.getUser();
            userService.updateUserById(user.setHeaderUrl(newHeadUrl));
            return "redirect:/index";
        }
    }

    @RequestMapping(value = "/user/header/{filename}", method = RequestMethod.GET)
    @ResponseBody
    public void getHeaderImage(@PathVariable("filename") String filename, HttpServletResponse response) {
        String path = uploadPath + filename;
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        response.setContentType("image/" + suffix);
        try (OutputStream outputStream = response.getOutputStream();
             InputStream inputStream = new FileInputStream(path)) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            log.error("读取文件失败：" + e.getMessage());
        }
    }

    @LoginCheck
    @RequestMapping(value = "/user/updatePassword", method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPassword, String newPassword) {
        if (StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oldPasswordMsg", "原始密码不能为空！");
            return "site/setting";
        }

        if (StringUtils.isBlank(newPassword)) {
            model.addAttribute("newPasswordMsg", "新始密码不能为空！");
            return "site/setting";
        }

        User user = HostHolder.getUser();
        String key = GeneralUtil.md5(oldPassword + user.getSalt());
        if (!key.equals(user.getPassword())) {
            model.addAttribute("oldPasswordMsg", "原始密码错误！");
            return "site/setting";
        }

        user.setSalt(GeneralUtil.generateUUID().substring(0, 5));
        user.setPassword(GeneralUtil.md5(newPassword + user.getSalt()));
        userService.updateUserById(user);

        return "redirect:/index";
    }

    @RequestMapping(value = "/user/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(Model model, @PathVariable("userId") int userId) {
        User user = userService.findUserById(userId);
        if (user == null)
            throw new RuntimeException("该用户不存在！");
        model.addAttribute("user", user);
        Map<String, Object> attributes = user.getAttributes();
        attributes.put("hasLikeCount", likeService.findUserLikeCount(userId));
        attributes.put("followeeCount", followService.findFolloweeCount(userId, ConstantUtil.ENTITY_TYPE_USER));
        attributes.put("followerCount", followService.findFollowerCount(ConstantUtil.ENTITY_TYPE_USER, userId));

        User me = HostHolder.getUser();
        if (me == null)
            attributes.put("isFollow", false);
        else
            attributes.put("isFollow", followService.isFollow(me.getId(), ConstantUtil.ENTITY_TYPE_USER, userId));
        return "site/profile";
    }
}
