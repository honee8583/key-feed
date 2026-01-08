package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class EmailVerificationAlreadyDoneException extends CustomException {

    public EmailVerificationAlreadyDoneException() {
        super(ErrorMessage.EMAIL_ALREADY_VERIFIED.getMessage(), HttpStatus.CONFLICT);
    }

}
