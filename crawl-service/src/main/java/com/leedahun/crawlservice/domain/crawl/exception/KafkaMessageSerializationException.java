package com.leedahun.crawlservice.domain.crawl.exception;

import static com.leedahun.crawlservice.common.message.ErrorMessage.KAFKA_MESSAGE_SERIALIZATION_FAIL;

public class KafkaMessageSerializationException extends RuntimeException {

    public KafkaMessageSerializationException() {
        super(KAFKA_MESSAGE_SERIALIZATION_FAIL.getMessage());
    }

    public KafkaMessageSerializationException(Throwable cause) {
        super(KAFKA_MESSAGE_SERIALIZATION_FAIL.getMessage(), cause);
    }

}
