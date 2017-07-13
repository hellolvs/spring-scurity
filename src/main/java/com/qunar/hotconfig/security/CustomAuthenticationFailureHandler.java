package com.qunar.hotconfig.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author shuai.lv
 * @date 2017/07/12.
 */
public class CustomAuthenticationFailureHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException, ServletException {
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath();
        String errMsg = URLEncoder.encode(e.getMessage(), "utf-8");
        // 如果request.getHeader("X-Requested-With") 返回的是"XMLHttpRequest"说明就是ajax请求，需要特殊处理
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            // 告诉ajax是重定向
            response.setHeader("REDIRECT", "REDIRECT");
            // 告诉ajax重定向的路径
            response.setHeader("CONTENTPATH", basePath + "/deniedView?errMsg=" + errMsg);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            response.sendRedirect(basePath + "/deniedView?errMsg=" + errMsg);
        }
    }
}
