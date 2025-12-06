package com.leedahun.matchservice.domain.content.service;

import com.leedahun.matchservice.domain.content.document.ContentDocument;
import com.leedahun.matchservice.domain.content.repository.ContentDocumentRepository;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentDocumentRepository contentDocumentRepository;

    @Transactional
    public void saveContent(CrawledContentDto dto) {

        ContentDocument contentDocument = ContentDocument.builder()
                .sourceId(dto.getSourceId())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .originalUrl(dto.getOriginalUrl())
                .thumbnailUrl(dto.getThumbnailUrl())
                .publishedAt(dto.getPublishedAt())
                .createdAt(LocalDateTime.now())
                .build();
        contentDocumentRepository.save(contentDocument);

        log.info("콘텐츠 저장 완료 (ES): {}", dto.getTitle());
    }
}