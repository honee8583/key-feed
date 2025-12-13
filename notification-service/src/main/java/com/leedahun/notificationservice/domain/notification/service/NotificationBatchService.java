package com.leedahun.notificationservice.domain.notification.service;

import com.leedahun.notificationservice.domain.notification.repository.NotificationJdbcRepository;
import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationBatchService {

    private final NotificationJdbcRepository notificationJdbcRepository;

    /**
     * Kafka에서 모아온 이벤트들을 한 번에 JDBC 배치로 저장
     */
    @Transactional
    public void saveBatch(List<NotificationEventDto> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        int[] result = notificationJdbcRepository.batchInsert(events);

        log.info("Notification batch insert completed. size={}, results[0]={}",
                events.size(),
                result.length > 0 ? result[0] : -1);
    }
}
