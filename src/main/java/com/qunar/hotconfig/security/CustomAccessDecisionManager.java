package com.qunar.hotconfig.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author shuai.lv
 * @date 2017/07/11.
 */
public class CustomAccessDecisionManager implements AccessDecisionManager {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAccessDecisionManager.class);

    private final RefreshAuthenticationTools refreshAuthenticationTools;

    @Autowired
    public CustomAccessDecisionManager(RefreshAuthenticationTools refreshAuthenticationTools) {
        this.refreshAuthenticationTools = refreshAuthenticationTools;
    }

    @Override
    public void decide(Authentication authentication, Object o, Collection<ConfigAttribute> attributes)
            throws AccessDeniedException, InsufficientAuthenticationException {

        //决策之前刷新用户权限信息
        refreshAuthenticationTools.refresh();

        if (null == attributes || attributes.size() == 0) {
            LOG.info("不需要权限认证！");
            return;
        }
        String errMsg = null;
        for (ConfigAttribute attribute : attributes) {
            String needRole = attribute.getAttribute();
            // authority为用户所被赋予的权限, needRole 为访问相应的资源应该具有的权限。
            for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
                if (needRole.equals(grantedAuthority.getAuthority())) {
                    LOG.info("{} 权限认证成功！", needRole);
                    return;
                }
            }
            if (needRole.equals("ROLE_READ")){
                errMsg = "没有查看权限！";
            }else if (needRole.equals("ROLE_ADMIN")){
                errMsg = "没有管理员权限！";
            }else if (needRole.contains("MODIFY")){
                errMsg = "没有该配置表的编辑权限！";
            }else if (needRole.contains("PUBLISH")){
                errMsg = "没有该配置表的发布权限！";
            }
        }

        throw new AccessDeniedException(errMsg);
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
