package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class UserAlreadyWithdrawnException extends CustomException {

    public UserAlreadyWithdrawnException() {
        super(ErrorMessage.USER_ALREADY_WITHDRAWN.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
