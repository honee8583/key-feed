package com.leedahun.notificationservice.infra.kafka.exception;

import static com.leedahun.notificationservice.common.message.ErrorMessage.KAFKA_MESSAGE_PROCESSING_ERROR;

public class KafkaMessageProcessingException extends RuntimeException {

    public KafkaMessageProcessingException() {
        super(KAFKA_MESSAGE_PROCESSING_ERROR.getMessage());
    }

}
