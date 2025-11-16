package com.leedahun.identityservice.common.error.exception;

import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class EntityAlreadyExistsException extends CustomException {

    public EntityAlreadyExistsException(String entity, Object data) {
        super(ErrorMessage.ENTITY_ALREADY_EXISTS.getMessage() + entity + ": " + data, HttpStatus.BAD_REQUEST);
    }

}
