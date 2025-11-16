package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class JwtTokenExpiredException extends CustomException {

    public JwtTokenExpiredException() {
        super(ErrorMessage.TOKEN_EXPIRED.getMessage(), HttpStatus.UNAUTHORIZED);
    }

}
