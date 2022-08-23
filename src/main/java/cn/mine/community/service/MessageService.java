package cn.mine.community.service;

import cn.mine.community.entity.Message;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface MessageService {
    PageInfo<Message> selectConversationsByPage(int userId, int pageNum, int pageSize);
    PageInfo<Message> selectLettersByPage(String conversationId, int pageNum, int pageSize);
    int selectConversationLettersCount(String conversationId);
    int selectUnreadLettersCount(int userId, String conversationId);
    int addLetter(Message message);
    int updateReadStatus(List<Integer> ids);
    int deleteLetter(int id);
    Message selectLatestNotice(int userId, String topic);
    int selectNoticeCount(int userId, String topic);
    int selectUnreadNoticesCount(int userId, String topic);
    PageInfo<Message> selectNoticesByPage(int userId, String topic, int pageNum, int pageSize);
}
