package com.leedahun.matchservice.domain.content.service;

import com.leedahun.matchservice.domain.content.entity.Content;
import com.leedahun.matchservice.domain.content.repository.ContentRepository;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @InjectMocks
    private ContentService contentService;

    @Mock
    private ContentRepository contentRepository;

    @Test
    @DisplayName("중복되지 않은 새로운 콘텐츠는 DB에 저장되어야 한다")
    void saveContent_Success_NewContent() {
        // given
        CrawledContentDto dto = CrawledContentDto.builder()
                .sourceId(1L)
                .title("New Title")
                .summary("Summary")
                .originalUrl("https://blog.com/post/1")
                .thumbnailUrl("https://img.com/1.jpg")
                .publishedAt(LocalDateTime.now())
                .build();

        // 중복 체크 시 false (존재하지 않음) 반환 설정
        when(contentRepository.existsBySourceIdAndOriginalUrl(dto.getSourceId(), dto.getOriginalUrl()))
                .thenReturn(false);

        // when
        contentService.saveContent(dto);

        // then
        // 중복 체크 메서드가 호출되었는지 확인
        verify(contentRepository, times(1))
                .existsBySourceIdAndOriginalUrl(dto.getSourceId(), dto.getOriginalUrl());

        // save 메서드가 1번 호출되었는지 확인 (저장 성공)
        verify(contentRepository, times(1)).save(any(Content.class));
    }

    @Test
    @DisplayName("이미 존재하는 콘텐츠(중복)는 저장하지 않고 로직을 종료해야 한다")
    void saveContent_Skip_Duplicate() {
        // given
        CrawledContentDto dto = CrawledContentDto.builder()
                .sourceId(1L)
                .title("Duplicate Title")
                .summary("Summary")
                .originalUrl("https://blog.com/post/1")
                .thumbnailUrl("https://img.com/1.jpg")
                .publishedAt(LocalDateTime.now())
                .build();

        // 중복 체크 시 true (이미 존재함) 반환 설정
        when(contentRepository.existsBySourceIdAndOriginalUrl(dto.getSourceId(), dto.getOriginalUrl()))
                .thenReturn(true);

        // when
        contentService.saveContent(dto);

        // then
        // 중복 체크 메서드가 호출되었는지 확인
        verify(contentRepository, times(1))
                .existsBySourceIdAndOriginalUrl(dto.getSourceId(), dto.getOriginalUrl());

        // 이미 존재하므로 save 메서드는 절대 호출되지 않아야 함
        verify(contentRepository, never()).save(any(Content.class));
    }
}