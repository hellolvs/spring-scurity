package com.qunar.hotconfig.security;

import com.google.common.collect.Sets;
import com.qunar.hotconfig.model.UserInfoModel;
import com.qunar.hotconfig.model.UserPermissionModel;
import com.qunar.hotconfig.service.UserPermissionService;
import com.qunar.hotconfig.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;

/**
 * @author shuai.lv
 * @date 2017/07/07.
 */
public class CustomUserDetailsService implements UserDetailsService {

    protected static Logger LOG = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserService userService;
    private final UserPermissionService userPermissionService;

    @Autowired
    public CustomUserDetailsService(UserService userService, UserPermissionService userPermissionService) {
        this.userService = userService;
        this.userPermissionService = userPermissionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {

        Set<GrantedAuthority> authoritySet = Sets.newHashSet();

        UserInfoModel userInfo = userService.getUserInfoByUserId(username);

        List<UserPermissionModel> userPermissions = userPermissionService.selectByUserId(username);

        if (userInfo != null){
            authoritySet.add(new SimpleGrantedAuthority("ROLE_READ"));  //有读权限

            if (userInfo.getGroupId().equals("1")){ //有管理员权限
                authoritySet.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            for (UserPermissionModel userPermission : userPermissions) {
                String fileId = userPermission.getFileId().toUpperCase();
                if (userPermission.getModifyPermission()){ //对表fileId有编辑权限
                    authoritySet.add(new SimpleGrantedAuthority("ROLE_" + fileId + "_MODIFY"));
                }
                if (userPermission.getPublishPermission()){ //对表fileId有发布权限
                    authoritySet.add(new SimpleGrantedAuthority("ROLE_" + fileId + "_PUBLISH"));
                }
            }
        }

        LOG.info("Granted Authorities to {}：", username);
        for (GrantedAuthority grantedAuthority : authoritySet) {
            LOG.info("{}", grantedAuthority.getAuthority());
        }

        return new User(username, "", authoritySet);
    }

}
