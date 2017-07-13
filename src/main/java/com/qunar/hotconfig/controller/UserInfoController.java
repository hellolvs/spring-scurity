package com.qunar.hotconfig.controller;

import com.qunar.flight.qmonitor.QMonitor;
import com.qunar.hotconfig.model.UserInfoModel;
import com.qunar.hotconfig.service.UserService;
import com.qunar.hotconfig.util.jsonUtil.JsonModel;
import com.qunar.security.QSSO;
import com.qunar.security.qsso.model.QUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by kun.ji on 2017/4/10.
 */
@Controller
@RequestMapping("")
public class UserInfoController {

    private static final Logger LOG = LoggerFactory.getLogger(UserInfoController.class);

    private final AuthenticationManager authManager;
    private final HttpServletRequest request;
    private final UserService userService;

    @Autowired
    public UserInfoController(HttpServletRequest request, UserService userService,
            @Qualifier("authenticationManager") AuthenticationManager authManager) {
        this.request = request;
        this.userService = userService;
        this.authManager = authManager;
    }

    @RequestMapping("/qlogin")
    public String login(HttpServletResponse response) {
        String token = request.getParameter("token");
        QUser user = QSSO.getQUser(token);
        UserInfoModel userInfo = userService.getUserInfoByUserId(user.getUserId());
        if (null == userInfo) {
            UserInfoModel newUser = userService.initNewUser(user);
            response.addCookie(userService.saveUserInfo(newUser));
        } else {
            response.addCookie(userService.saveUserInfo(userInfo));
        }

        UsernamePasswordAuthenticationToken autoToken = new UsernamePasswordAuthenticationToken(user.getUserId(), "");
        // 用户名密码认证，会调用loadUserByUsername对比，返回authResult（用户认证信息）
        Authentication authResult = authManager.authenticate(autoToken);

        // 在 session 中保存 authResult
        // sessionStrategy.onAuthentication(authResult, request, response);

        // 在当前访问线程的ThreadLocal中设置 authResult
        SecurityContextHolder.getContext().setAuthentication(authResult);

        QMonitor.recordOne("Login_Success");
        LOG.info("用户 {} 已登录", user.getUserId());

        return "redirect:/configView";
    }

    /* 异常处理，输出异常信息 */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public JsonModel handlerRuntimeException(RuntimeException e) {
        LOG.error("UserInfoController运行时异常：{}", e.getMessage(), e);
        return JsonModel.errJsonModel("无法提供该服务");
    }
}