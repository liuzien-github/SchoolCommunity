package cn.mine.community.controller;

import cn.mine.community.annotation.LoginCheck;
import cn.mine.community.service.LoginTicketService;
import cn.mine.community.util.CookieUtil;
import cn.mine.community.util.GeneralUtil;
import cn.mine.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class LoginController {
    @Value("${community.login-expired-seconds.default}")
    private String defaultSpiredSeconds;

    @Value("${community.login-expired-seconds.remember}")
    private String rememberSpiredSeconds;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private LoginTicketService loginTicketService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "site/login";
    }

    @RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
    @ResponseBody
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        String kaptchaOwner = GeneralUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        String key = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(key, text, 60, TimeUnit.SECONDS);

        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            log.error("响应验证码失败：" + e.getMessage());
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(HttpServletResponse response, HttpServletRequest request, Model model,
                        String username, String password, String code, boolean rememberMe) {

        String kaptchaOwner = CookieUtil.getValue(request, "kaptchaOwner");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String key = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(key);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码有错误或者已经过期！");
            return "site/login";
        }

        long expiredSeconds = rememberMe ? Long.parseLong(rememberSpiredSeconds) : Long.parseLong(defaultSpiredSeconds);
        Map<String, Object> map = loginTicketService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);    //确保cookie在任意路径下都可以被获得
            cookie.setMaxAge((int) expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    @LoginCheck
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        loginTicketService.logout(ticket);
        return "redirect:/login";
    }
}
