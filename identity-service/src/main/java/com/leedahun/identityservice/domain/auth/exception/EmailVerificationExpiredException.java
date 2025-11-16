package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class EmailVerificationExpiredException extends CustomException {

    public EmailVerificationExpiredException() {
        super(ErrorMessage.EMAIL_VERIFICATION_EXPIRED.getMessage(), HttpStatus.GONE);
    }

}
