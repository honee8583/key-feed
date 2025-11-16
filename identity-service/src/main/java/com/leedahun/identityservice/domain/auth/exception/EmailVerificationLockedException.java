package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class EmailVerificationLockedException extends CustomException {

    public EmailVerificationLockedException() {
        super(ErrorMessage.EMAIL_VERIFICATION_LOCKED.getMessage(), HttpStatus.LOCKED);
    }

}
