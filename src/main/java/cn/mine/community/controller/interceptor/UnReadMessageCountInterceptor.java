package cn.mine.community.controller.interceptor;

import cn.mine.community.entity.User;
import cn.mine.community.service.MessageService;
import cn.mine.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UnReadMessageCountInterceptor implements HandlerInterceptor {
    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = HostHolder.getUser();
        if (user != null && modelAndView != null) {
            int unReadNoticesCount = messageService.selectUnreadNoticesCount(user.getId(), null);
            int unReadLettersCount = messageService.selectUnreadLettersCount(user.getId(), null);
            modelAndView.addObject("allUnReadCount", unReadLettersCount + unReadNoticesCount);
        }
    }
}
