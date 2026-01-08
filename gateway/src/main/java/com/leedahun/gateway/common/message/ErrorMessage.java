package com.leedahun.gateway.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage {
    TOKEN_EXPIRED("토큰이 만료되었습니다."),
    TOKEN_INVALID("토큰이 유효하지 않습니다.");

    private final String message;
}
