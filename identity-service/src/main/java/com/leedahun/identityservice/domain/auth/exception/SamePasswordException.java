package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class SamePasswordException extends CustomException {

    public SamePasswordException() {
        super(ErrorMessage.SAME_PASSWORD.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
