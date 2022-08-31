package cn.mine.community.controller.interceptor;

import cn.mine.community.entity.User;
import cn.mine.community.service.DataService;
import cn.mine.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        User user = HostHolder.getUser();
        if (user != null)
            dataService.recordDAU(user.getId());

        return true;
    }
}
