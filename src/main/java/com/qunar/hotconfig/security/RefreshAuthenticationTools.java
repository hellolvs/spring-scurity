package com.qunar.hotconfig.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 用于页面刷新时刷新权限认证
 * @author shuai.lv
 * @date 2017/07/12.
 */
@Component
public class RefreshAuthenticationTools {

    private final AuthenticationManager authManager;

    @Autowired
    public RefreshAuthenticationTools(@Qualifier("authenticationManager") AuthenticationManager authManager) {
        this.authManager = authManager;
    }


    public void refresh(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UsernamePasswordAuthenticationToken autoToken = new UsernamePasswordAuthenticationToken(username, "");
        // 用户名密码认证，会调用loadUserByUsername对比，返回authResult（用户认证信息）
        Authentication authResult = authManager.authenticate(autoToken);

        // 在当前访问线程的ThreadLocal中设置 authResult
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }
}
