package com.leedahun.identityservice.domain.source.exception;

import static com.leedahun.identityservice.common.message.ErrorMessage.INVALID_RSS_URL;

import com.leedahun.identityservice.common.error.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidRssUrlException extends CustomException {

    public InvalidRssUrlException() {
        super(INVALID_RSS_URL.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
