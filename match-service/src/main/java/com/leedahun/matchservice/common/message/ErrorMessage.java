package com.leedahun.matchservice.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage {
    INTERNAL_SERVER_ERROR("서버 에러가 발생했습니다."),
    ENTITY_NOT_FOUND("데이터가 존재하지 않습니다. "),
    ENTITY_ALREADY_EXISTS("데이터가 이미 존재합니다. "),
    INVALID_INPUT_VALUE("입력값이 올바르지 않습니다."),

    UNAUTHORIZED("인증이 필요합니다."),
    FORBIDDEN("권한이 없습니다."),

    KEYWORD_REQUEST_FAIL("키워드 목록 조회에 실패하였습니다."),

    KAFKA_MESSAGE_PROCESSING_ERROR("카프카 메시지 처리 중 에러가 발생하였습니다.");

    private final String message;
}
