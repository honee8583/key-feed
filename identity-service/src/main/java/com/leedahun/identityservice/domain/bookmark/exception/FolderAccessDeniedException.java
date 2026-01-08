package com.leedahun.identityservice.domain.bookmark.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class FolderAccessDeniedException extends CustomException {

    public FolderAccessDeniedException() {
        super(ErrorMessage.FORBIDDEN.getMessage(), HttpStatus.FORBIDDEN);
    }
}