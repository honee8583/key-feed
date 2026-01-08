package com.leedahun.notificationservice.domain.notification.service;

import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventBuffer {

    private final NotificationBatchService batchService;

    @Value("${jdbc.batch.size}")
    private int FLUSH_BATCH_SIZE;

    private final Queue<NotificationEventDto> queue = new ConcurrentLinkedQueue<>();

    public void add(NotificationEventDto event) {
        queue.offer(event);
    }

    @Scheduled(fixedDelay = 100)
    public void flush() {
        List<NotificationEventDto> data = new ArrayList<>(FLUSH_BATCH_SIZE);

        while (data.size() < FLUSH_BATCH_SIZE) {
            NotificationEventDto e = queue.poll();
            if (e == null) {
                break;
            }
            data.add(e);
        }

        if (data.isEmpty()) {
            return;
        }

        try {
            batchService.saveBatch(data);
        } catch (Exception e) {
            log.error("Notification JDBC batch insert error, size={}", data.size(), e);
        }
    }
}