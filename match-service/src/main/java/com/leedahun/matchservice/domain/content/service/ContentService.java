package com.leedahun.matchservice.domain.content.service;

import com.leedahun.matchservice.domain.content.entity.Content;
import com.leedahun.matchservice.domain.content.repository.ContentRepository;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    @Transactional
    public void saveContent(CrawledContentDto dto) {
        // 중복 체크
        if (contentRepository.existsBySourceIdAndOriginalUrl(dto.getSourceId(), dto.getOriginalUrl())) {
            log.info("이미 저장된 콘텐츠입니다. (Skip): {}", dto.getTitle());
            return;
        }

        Content content = Content.builder()
                .sourceId(dto.getSourceId())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .originalUrl(dto.getOriginalUrl())
                .thumbnailUrl(dto.getThumbnailUrl())
                .publishedAt(dto.getPublishedAt())
                .build();
        contentRepository.save(content);
    }
}