package cn.mine.community;

import cn.mine.community.entity.LoginTicket;
import cn.mine.community.util.GeneralUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommunityApplication.class)
public class CommunityApplicationTests {
	/*@Autowired
	private RedisTemplate redisTemplate;*/

	@Test
	public void contextLoads() {
		/*LoginTicket loginTicket = new LoginTicket()
				.setUserId(123)
				.setTicket(GeneralUtil.generateUUID())
				.setStatus(0)
				.setExpired(new Date());
		String ticket = GeneralUtil.generateUUID();
		redisTemplate.opsForValue().set(ticket, loginTicket, 30, TimeUnit.SECONDS);
		loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticket);
		System.out.println(loginTicket.toString());
		redisTemplate.delete(ticket);*/
	}
}
