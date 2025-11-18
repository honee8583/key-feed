package com.leedahun.identityservice.domain.auth.util.test;

import com.leedahun.identityservice.domain.auth.dto.LoginUser;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithAnonymousUserSecurityContextFactory implements WithSecurityContextFactory<WithAnonymousUser> {

    @Override
    public SecurityContext createSecurityContext(WithAnonymousUser annotation) {
        String username = annotation.username();
        String role = annotation.role();

        LoginUser user = new LoginUser(1L, "user", "11111111", "ROLE_USER");

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user, "password", List.of(new SimpleGrantedAuthority(role)));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
        return context;
    }
}
