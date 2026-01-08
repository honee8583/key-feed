package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class EmailVerificationAttemptLimitExceededException extends CustomException {

    public EmailVerificationAttemptLimitExceededException() {
        super(ErrorMessage.EMAIL_VERIFICATION_ATTEMPT_LIMIT_EXCEEDED.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

}
