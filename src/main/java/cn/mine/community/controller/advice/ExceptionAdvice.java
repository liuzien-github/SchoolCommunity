package cn.mine.community.controller.advice;

import cn.mine.community.util.GeneralUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    @ExceptionHandler(Exception.class)
    public void handle(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("服务器发生异常：" + e.getMessage());
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            log.error(element.toString());
        }

        String xHttpRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xHttpRequestedWith)) {
            response.setContentType("application/plain;charset=utf-8");
            response.getWriter().write(GeneralUtil.getJsonString(1, "服务器异常！"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
