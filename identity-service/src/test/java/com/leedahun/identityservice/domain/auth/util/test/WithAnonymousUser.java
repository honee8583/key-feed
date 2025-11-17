package com.leedahun.identityservice.domain.auth.util.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAnonymousUserSecurityContextFactory.class)
public @interface WithAnonymousUser {
    String id() default "";
}
