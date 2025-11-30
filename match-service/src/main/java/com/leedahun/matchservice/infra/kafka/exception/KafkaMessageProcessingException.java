package com.leedahun.matchservice.infra.kafka.exception;

import static com.leedahun.matchservice.common.message.ErrorMessage.KAFKA_MESSAGE_PROCESSING_ERROR;

public class KafkaMessageProcessingException extends RuntimeException {

    public KafkaMessageProcessingException() {
        super(KAFKA_MESSAGE_PROCESSING_ERROR.getMessage());
    }

}
