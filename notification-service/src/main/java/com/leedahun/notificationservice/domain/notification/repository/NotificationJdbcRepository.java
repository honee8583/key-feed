package com.leedahun.notificationservice.domain.notification.repository;

import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = """
                INSERT INTO notification (notification_id, user_id, title, message, original_url, created_at) VALUES (?, ?, ?, ?, ?, ?)
            """;

    public int[] batchInsert(List<NotificationEventDto> events) {
        if (events == null || events.isEmpty()) {
            return new int[0];
        }

        LocalDateTime now = LocalDateTime.now();
        int[] result = jdbcTemplate.batchUpdate(
                INSERT_SQL,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        NotificationEventDto e = events.get(i);
                        ps.setLong(1, e.getNotificationId());
                        ps.setLong(2, e.getUserId());
                        ps.setString(3, e.getTitle());
                        ps.setString(4, e.getMessage());
                        ps.setString(5, e.getOriginalUrl());
                        ps.setTimestamp(6, Timestamp.valueOf(now));
                    }

                    @Override
                    public int getBatchSize() {
                        return events.size();
                    }
                }
        );

        return result;
    }
}
