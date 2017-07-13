package com.qunar.hotconfig.security;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 获得URL资源对应的权限关系
 * 
 * @author shuai.lv
 * @date 2017/07/11.
 */
public class CustomSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    private static final Logger LOG = LoggerFactory.getLogger(CustomSecurityMetadataSource.class);

    private static final String CONFIG_VIEW = "/configView";
    private static final String PERMISSION_VIEW = "/permissionView";
    private static final String PERMISSION_URL = "/manageUserPermission/CurrentUserPermission";
    private static final String CONFIG_URL = "/manageConfFiles/CurrentConfData";

    private static final Splitter SPLITTER = Splitter.on("/").omitEmptyStrings().trimResults();

    /* 通过url地址获取权限信息的方法 */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        // 获取当前的URL地址
        FilterInvocation filterInvocation = (FilterInvocation) object;
        String url = filterInvocation.getRequestUrl();
        List<String> urls = Lists.newArrayList(SPLITTER.split(url));
        LOG.info("访问的URL：{}", url);

        Set<ConfigAttribute> attributes = Sets.newHashSet();

        if (url.equals(CONFIG_VIEW)){
            attributes.add(new SecurityConfig("ROLE_READ"));
        }else if (url.contains(PERMISSION_URL) || url.equals(PERMISSION_VIEW)){
            attributes.add(new SecurityConfig("ROLE_ADMIN"));
        }else if (url.contains(CONFIG_URL)){
            if (url.contains("add") ||url.contains("delete")||url.contains("update")){
                attributes.add(new SecurityConfig("ROLE_" + urls.get(2).toUpperCase() + "_MODIFY"));
            }else if (url.contains("publish")){
                attributes.add(new SecurityConfig("ROLE_" + urls.get(2).toUpperCase() + "_PUBLISH"));
            }else {
                attributes.add(new SecurityConfig("ROLE_READ"));
            }
        }

        LOG.info("需要的权限：{}", attributes);
        return attributes;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* 如果为真则说明支持当前格式类型,才会到上面的 getAttributes 方法中 */
    @Override
    public boolean supports(Class<?> clazz) {
        // TODO Auto-generated method stub
        // 需要返回true表示支持
        return true;
    }

}
