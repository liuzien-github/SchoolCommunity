package cn.mine.community.event;

import cn.mine.community.entity.DiscussPost;
import cn.mine.community.entity.Event;
import cn.mine.community.entity.Message;
import cn.mine.community.service.DiscussPostService;
import cn.mine.community.service.ElasticsearchService;
import cn.mine.community.service.MessageService;
import cn.mine.community.util.ConstantUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class EventConsumer {
    @Value("${community.wk.image.storage}")
    private String storage;

    @Value("${community.wk.image.command}")
    private String command;

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {ConstantUtil.TOPIC_COMMENT, ConstantUtil.TOPIC_FOLLOW, ConstantUtil.TOPIC_LIKE})
    public void handleCFLMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            log.error("消息内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误！");
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("userId", event.getUserId());
        map.put("entityType", event.getEntityType());
        map.put("entityId", event.getEntityId());
        Map<String, Object> data = event.getAttributes();
        if (!data.isEmpty()) {
            Set<Map.Entry<String, Object>> entries = data.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        Message message = new Message()
                .setFromId(ConstantUtil.SYSTEM_USER_ID)
                .setToId(event.getEntityUserId())
                .setConversationId(event.getTopic())
                .setStatus(0)
                .setCreateTime(new Date())
                .setContent(JSONObject.toJSONString(map));

        messageService.addLetter(message);
    }

    @KafkaListener(topics = {ConstantUtil.TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            log.error("消息内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误！");
            return;
        }

        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }

    @KafkaListener(topics = {ConstantUtil.TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            log.error("消息内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误！");
            return;
        }

        elasticsearchService.deleteDiscussPostById(event.getEntityId());
    }

    @KafkaListener(topics = {ConstantUtil.TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            log.error("消息内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误！");
            return;
        }

        Map<String, Object> attributes = event.getAttributes();
        String htmlUrl = (String) attributes.get("htmlUrl");
        String fileName = (String) attributes.get("fileName");
        String suffix = (String) attributes.get("suffix");

        String cmd = command + " --quality 75 " + htmlUrl + " " + storage + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            log.info("生成长图成功：" + cmd);
        } catch (IOException e) {
            log.error("生成长图失败：" + e.getMessage());
        }
    }
}
