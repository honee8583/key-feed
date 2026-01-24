package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class PasswordMismatchException extends CustomException {

    public PasswordMismatchException() {
        super(ErrorMessage.PASSWORD_MISMATCH.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
