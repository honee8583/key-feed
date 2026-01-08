package com.leedahun.crawlservice.common.filter;

import static com.leedahun.crawlservice.common.message.ErrorMessage.FORBIDDEN;

import com.leedahun.crawlservice.common.util.AuthenticationResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        AuthenticationResponseUtil.authenticateFail(response, HttpStatus.FORBIDDEN, FORBIDDEN.getMessage());
    }

}
