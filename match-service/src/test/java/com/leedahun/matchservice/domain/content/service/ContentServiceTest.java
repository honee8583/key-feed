package com.leedahun.matchservice.domain.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leedahun.matchservice.domain.content.entity.Content;
import com.leedahun.matchservice.domain.content.repository.ContentRepository;
import com.leedahun.matchservice.domain.content.service.impl.ContentServiceImpl;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @InjectMocks
    private ContentServiceImpl contentService;

    @Mock
    private ContentRepository contentRepository;

    @Test
    @DisplayName("존재하지 않는 콘텐츠는 MySQL에 저장된다")
    void saveContent_NewContent_Saved() {
        // given
        CrawledContentDto dto = CrawledContentDto.builder()
                .sourceId(1L)
                .title("New Title")
                .summary("Summary")
                .originalUrl("https://blog.com/post/1")
                .thumbnailUrl("https://img.com/1.jpg")
                .publishedAt(LocalDateTime.now())
                .build();

        when(contentRepository.existsBySourceIdAndOriginalUrl(anyLong(), anyString()))
                .thenReturn(false);

        // when
        contentService.saveContent(dto);

        // then
        verify(contentRepository, times(1)).save(any(Content.class));
    }

    @Test
    @DisplayName("이미 존재하는 콘텐츠는 저장하지 않고 skip한다")
    void saveContent_DuplicateContent_Skipped() {
        // given
        CrawledContentDto dto = CrawledContentDto.builder()
                .sourceId(1L)
                .title("Duplicate Title")
                .summary("Summary")
                .originalUrl("https://blog.com/post/1")
                .thumbnailUrl("https://img.com/1.jpg")
                .publishedAt(LocalDateTime.now())
                .build();

        when(contentRepository.existsBySourceIdAndOriginalUrl(anyLong(), anyString()))
                .thenReturn(true);

        // when
        contentService.saveContent(dto);

        // then
        verify(contentRepository, never()).save(any());
    }

    @Test
    @DisplayName("저장 시 Content 엔티티에 올바른 필드가 매핑된다")
    void saveContent_ContentFields_MappedCorrectly() {
        // given
        LocalDateTime publishedAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        CrawledContentDto dto = CrawledContentDto.builder()
                .sourceId(1L)
                .title("Test Title")
                .summary("Test Summary")
                .originalUrl("https://blog.com/post/1")
                .thumbnailUrl("https://img.com/1.jpg")
                .publishedAt(publishedAt)
                .build();

        when(contentRepository.existsBySourceIdAndOriginalUrl(anyLong(), anyString()))
                .thenReturn(false);

        ArgumentCaptor<Content> captor = ArgumentCaptor.forClass(Content.class);

        // when
        contentService.saveContent(dto);

        // then
        verify(contentRepository).save(captor.capture());
        Content saved = captor.getValue();
        assertThat(saved.getSourceId()).isEqualTo(1L);
        assertThat(saved.getTitle()).isEqualTo("Test Title");
        assertThat(saved.getSummary()).isEqualTo("Test Summary");
        assertThat(saved.getOriginalUrl()).isEqualTo("https://blog.com/post/1");
        assertThat(saved.getThumbnailUrl()).isEqualTo("https://img.com/1.jpg");
        assertThat(saved.getPublishedAt()).isEqualTo(publishedAt);
    }
}
