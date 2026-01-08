package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class RefreshTokenNotExistsException extends CustomException {

    public RefreshTokenNotExistsException() {
        super(ErrorMessage.EMPTY_TOKEN.getMessage(), HttpStatus.UNAUTHORIZED);
    }

}
