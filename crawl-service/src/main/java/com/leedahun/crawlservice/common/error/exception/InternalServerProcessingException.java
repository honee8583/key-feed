package com.leedahun.crawlservice.common.error.exception;

import com.leedahun.crawlservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class InternalServerProcessingException extends CustomException {

    public InternalServerProcessingException() {
        super(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerProcessingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
