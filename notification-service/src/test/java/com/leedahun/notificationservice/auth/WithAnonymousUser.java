package com.leedahun.notificationservice.auth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAnonymousUserSecurityContextFactory.class)
public @interface WithAnonymousUser {
//    String username() default "1";
    String userId() default "1";
    String role() default "ROLE_USER";
}
