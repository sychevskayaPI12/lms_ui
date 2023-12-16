package com.anast.lms.service.security;

import com.anast.lms.model.UserAuthInfo;
import com.anast.lms.service.external.UserServiceClient;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("authService")
public class AuthUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    public AuthUserDetailsService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuthInfo userAuthInfo = userServiceClient.getUserAuthInfoByLogin(username);
        return User.withUsername(username)
                .password(userAuthInfo.getPassword())
                .roles(userAuthInfo.getRoles().toArray(new String[0]))
                .build();
    }
}
