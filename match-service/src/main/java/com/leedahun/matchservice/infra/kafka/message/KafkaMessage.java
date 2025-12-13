package com.leedahun.matchservice.infra.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KafkaMessage {
    NOTIFICATION_MESSAGE("등록한 키워드의 게시글이 올라왔습니다.");

    private final String message;
}