package com.leedahun.identityservice.domain.bookmark.exception;

import com.leedahun.identityservice.common.error.exception.CustomException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class FolderLimitExceededException extends CustomException {
    public FolderLimitExceededException() {
        super(ErrorMessage.BOOKMARK_FOLDER_LIMIT_EXCEEDED.getMessage(), HttpStatus.BAD_REQUEST);
    }
}