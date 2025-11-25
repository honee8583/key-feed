package com.leedahun.crawlservice.common.error.exception;

import com.leedahun.crawlservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends CustomException {

    public EntityNotFoundException(String entity, Object data) {
        super(ErrorMessage.ENTITY_NOT_FOUND.getMessage() + entity + ": " + data, HttpStatus.CONFLICT);
    }

}
