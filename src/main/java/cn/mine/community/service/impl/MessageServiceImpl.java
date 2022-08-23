package cn.mine.community.service.impl;

import cn.mine.community.dao.MessageMapper;
import cn.mine.community.entity.Message;
import cn.mine.community.service.MessageService;
import cn.mine.community.util.SensitiveWordsFilter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveWordsFilter filter;

    @Override
    public PageInfo<Message> selectConversationsByPage(int userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Message> messages = messageMapper.selectConversations(userId);
        return new PageInfo<>(messages);
    }

    @Override
    public PageInfo<Message> selectLettersByPage(@NotNull String conversationId, int pageNum, int pageSize) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2)
                .ne("from_id", 1)
                .eq("conversation_id", conversationId)
                .orderByDesc("id");

        PageHelper.startPage(pageNum, pageSize);
        List<Message> messages = messageMapper.selectList(queryWrapper);
        return new PageInfo<>(messages);
    }

    @Override
    public int selectConversationLettersCount(@NotNull String conversationId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2)
                .ne("from_id", 1)
                .eq("conversation_id", conversationId);
        return messageMapper.selectCount(queryWrapper);
    }

    @Override
    public int selectUnreadLettersCount(int userId, String conversationId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("from_id", 1)
                .eq("status", 0)
                .eq("to_id", userId);

        if (conversationId != null)
            queryWrapper.eq("conversation_id", conversationId);

        return messageMapper.selectCount(queryWrapper);
    }

    @Override
    public int addLetter(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(filter.filter(message.getContent()));
        return messageMapper.insert(message);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public int updateReadStatus(List<Integer> ids) {
        int rows = 0;
        if (ids != null) {
            rows = ids.size();
            Message message = new Message().setStatus(1);
            for (int id : ids) {
                message.setId(id);
                messageMapper.updateById(message);
            }
        }
        return rows;
    }

    @Override
    public int deleteLetter(int id) {
        Message message = new Message()
                .setId(id)
                .setStatus(2);
        return messageMapper.updateById(message);
    }

    @Override
    public Message selectLatestNotice(int userId, String topic) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2)
                .eq("from_id", 1)
                .eq("to_id", userId)
                .eq("conversation_id", topic)
                .orderByDesc("id");
        List<Message> messages = messageMapper.selectList(queryWrapper);
        if (messages == null || messages.isEmpty())
            return null;
        return messages.get(0);
    }

    @Override
    public int selectNoticeCount(int userId, String topic) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2)
                .eq("from_id", 1)
                .eq("to_id", userId)
                .eq("conversation_id", topic);
        return messageMapper.selectCount(queryWrapper);
    }

    @Override
    public int selectUnreadNoticesCount(int userId, String topic) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0)
                .eq("from_id", 1)
                .eq("to_id", userId);
        if (topic != null)
                queryWrapper.eq("conversation_id", topic);
        return messageMapper.selectCount(queryWrapper);
    }

    @Override
    public PageInfo<Message> selectNoticesByPage(int userId, String topic, int pageNum, int pageSize) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2)
                .eq("from_id", 1)
                .eq("to_id", userId)
                .eq("conversation_id", topic)
                .orderByDesc("id");

        PageHelper.startPage(pageNum, pageSize);
        List<Message> messages = messageMapper.selectList(queryWrapper);
        return new PageInfo<>(messages);
    }
}
