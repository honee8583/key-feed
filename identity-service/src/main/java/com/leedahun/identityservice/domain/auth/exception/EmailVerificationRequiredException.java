package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class EmailVerificationRequiredException extends CustomException {

    public EmailVerificationRequiredException() {
        super(ErrorMessage.EMAIL_VERIFICATION_REQUIRED.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
