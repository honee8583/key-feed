package com.leedahun.matchservice.domain.content.service.impl;

import com.leedahun.matchservice.domain.content.entity.Content;
import com.leedahun.matchservice.domain.content.repository.ContentRepository;
import com.leedahun.matchservice.domain.content.service.ContentService;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;

    @Transactional
    public void saveContent(CrawledContentDto dto) {
        if (contentRepository.existsBySourceIdAndOriginalUrl(dto.getSourceId(), dto.getOriginalUrl())) {
            log.info("이미 존재하는 콘텐츠 (skip): {}", dto.getOriginalUrl());
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

        log.info("콘텐츠 저장 완료 : source: {}, 제목: {}", dto.getSourceId(), dto.getTitle());
    }
}