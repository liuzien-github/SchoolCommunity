package cn.mine.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
	@PostConstruct
	public void init() {
		/**
		 * es底层用到netty,当其它组件也使用netty时，会发生冲突
		 * 查看org.elasticsearch.transport.netty4.Netty4Utils#setAvailableProcessors(int)
		 */
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}
}
