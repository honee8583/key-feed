package com.leedahun.feedservice.common.error.exception;

import com.leedahun.feedservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class InternalServerProcessingException extends CustomException {

    public InternalServerProcessingException() {
        super(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerProcessingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
