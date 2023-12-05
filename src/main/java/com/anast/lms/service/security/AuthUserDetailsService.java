package com.anast.lms.service.security;

import com.anast.lms.model.UserAuthInfo;
import com.anast.lms.service.external.UserServiceClient;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    public AuthUserDetailsService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //по идее, отсюда обращаемся в бд, получаем юзера с его паролем и ролями.
        //формочка сверит эти данные с теми, что были введены
        UserAuthInfo userAuthInfo = userServiceClient.getUserAuthInfoByLogin(username);
        return User.withUsername(username)
                .password("{noop}" + userAuthInfo.getPassword())
                .roles("USER")
                .build();
    }
}
