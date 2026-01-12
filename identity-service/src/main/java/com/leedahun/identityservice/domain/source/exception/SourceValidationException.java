package com.leedahun.identityservice.domain.source.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SourceValidationException extends CustomException {

    public SourceValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}