package cn.mine.community.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
@Slf4j
public class WKConfig {
    @Value("${community.wk.image.storage}")
    private String imageStorage;

    @PostConstruct
    public void init() {
        File file = new File(imageStorage);
        if (!file.exists()) {
            file.mkdirs();
            log.info("创建WK图片目录：" + imageStorage);
        }
    }
}
