package com.leedahun.feedservice.auth;

import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithAnonymousUserSecurityContextFactory implements WithSecurityContextFactory<WithAnonymousUser> {

    @Override
    public SecurityContext createSecurityContext(WithAnonymousUser annotation) {
        Long userId = Long.parseLong(annotation.userId());
        String role = annotation.role();

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userId, "password", List.of(new SimpleGrantedAuthority(role)));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
        return context;
    }
}
