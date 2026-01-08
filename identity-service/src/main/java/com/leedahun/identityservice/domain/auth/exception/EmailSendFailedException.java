package com.leedahun.identityservice.domain.auth.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class EmailSendFailedException extends CustomException {

    public EmailSendFailedException() {
        super(ErrorMessage.EMAIL_SEND_FAILED.getMessage(), HttpStatus.BAD_GATEWAY);
    }

}
