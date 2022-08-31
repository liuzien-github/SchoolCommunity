package cn.mine.community.controller;

import cn.mine.community.entity.Event;
import cn.mine.community.event.EventProducer;
import cn.mine.community.util.ConstantUtil;
import cn.mine.community.util.GeneralUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class ShareController {
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.wk.image.storage}")
    private String storage;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(value = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl) {
        String fileName = GeneralUtil.generateUUID();

        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_SHARE);
        Map<String, Object> attributes = event.getAttributes();
        attributes.put("htmlUrl", htmlUrl);
        attributes.put("fileName", fileName);
        attributes.put("suffix", ".png");
        eventProducer.fireEvent(event);

        Map<String, Object> map = new HashMap<>();
        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        return GeneralUtil.getJsonString(0, null, map);
    }

    @RequestMapping(value = "/share/image/{fileName}", method = RequestMethod.GET)
    @ResponseBody
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName))
            throw new IllegalArgumentException("文件名不能为空！");

        response.setContentType("image/png");
        File file = new File(storage + fileName + ".png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            log.error("获取图片失败：" + e.getMessage());
        }
    }
}
