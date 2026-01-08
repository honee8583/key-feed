package com.leedahun.matchservice.domain.content.service;

import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;

public interface NotificationTriggerService {

    void matchAndSendNotification(CrawledContentDto content);

}
