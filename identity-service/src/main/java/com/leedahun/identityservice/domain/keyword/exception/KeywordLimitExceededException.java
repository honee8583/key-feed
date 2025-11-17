package com.leedahun.identityservice.domain.keyword.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class KeywordLimitExceededException extends CustomException {

    public KeywordLimitExceededException() {
        super(ErrorMessage.KEYWORD_LIMIT_EXCEEDED.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
