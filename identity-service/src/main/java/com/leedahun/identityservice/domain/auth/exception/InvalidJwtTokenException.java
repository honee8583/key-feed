package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class InvalidJwtTokenException extends CustomException {

    public InvalidJwtTokenException() {
        super(ErrorMessage.INVALID_TOKEN.getMessage(), HttpStatus.UNAUTHORIZED);
    }

}
