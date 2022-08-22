package cn.mine.community;

import cn.mine.community.dao.MessageMapper;
import cn.mine.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommunityApplication.class)
public class CommunityApplicationTests {
	@Autowired
	private MessageMapper messageMapper;

	@Test
	public void contextLoads() {
		List<Message> messages = messageMapper.selectConversations(111);
		if (messages != null) {
			for (Message message : messages)
				System.out.println(message);
		}
	}
}
