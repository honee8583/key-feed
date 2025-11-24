package com.leedahun.identityservice.common.error.exception;

import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends CustomException {

    public EntityNotFoundException(String entity, Object data) {
        super(
                String.format("%s %s: %s", ErrorMessage.ENTITY_NOT_FOUND.getMessage(), entity, data),
                HttpStatus.NOT_FOUND
        );
    }

}
