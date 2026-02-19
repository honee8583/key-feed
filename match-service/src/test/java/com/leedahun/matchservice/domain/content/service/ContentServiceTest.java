package com.leedahun.matchservice.domain.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.leedahun.matchservice.domain.content.document.ContentDocument;
import com.leedahun.matchservice.domain.content.repository.ContentDocumentRepository;
import com.leedahun.matchservice.domain.content.service.impl.ContentServiceImpl;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import java.time.LocalDateTime;
import java.util.List;
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
    private ContentDocumentRepository contentDocumentRepository;

    @Test
    @DisplayName("콘텐츠 저장 시 ES 문서에 null이 아닌 ID가 설정되어야 한다")
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

        ArgumentCaptor<ContentDocument> captor = ArgumentCaptor.forClass(ContentDocument.class);

        // when
        contentService.saveContent(dto);

        // then
        verify(contentDocumentRepository, times(1)).save(captor.capture());
        ContentDocument saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("동일한 originalUrl로 두 번 저장해도 동일한 ID의 문서가 생성되어야 한다 (멱등성)")
    void saveContent_Idempotent_SameUrlProducesSameId() {
        // given
        CrawledContentDto dto = CrawledContentDto.builder()
                .sourceId(1L)
                .title("Same Article")
                .summary("Summary")
                .originalUrl("https://blog.com/post/1")
                .thumbnailUrl("https://img.com/1.jpg")
                .publishedAt(LocalDateTime.now())
                .build();

        ArgumentCaptor<ContentDocument> captor = ArgumentCaptor.forClass(ContentDocument.class);

        // when
        contentService.saveContent(dto);
        contentService.saveContent(dto);

        // then
        verify(contentDocumentRepository, times(2)).save(captor.capture());
        List<ContentDocument> savedDocs = captor.getAllValues();
        assertThat(savedDocs.get(0).getId()).isEqualTo(savedDocs.get(1).getId());
    }

    @Test
    @DisplayName("서로 다른 originalUrl은 서로 다른 ID를 생성해야 한다")
    void saveContent_DifferentUrls_ProduceDifferentIds() {
        // given
        CrawledContentDto dto1 = CrawledContentDto.builder()
                .sourceId(1L)
                .title("Article 1")
                .summary("Summary 1")
                .originalUrl("https://blog.com/post/1")
                .thumbnailUrl("https://img.com/1.jpg")
                .publishedAt(LocalDateTime.now())
                .build();

        CrawledContentDto dto2 = CrawledContentDto.builder()
                .sourceId(1L)
                .title("Article 2")
                .summary("Summary 2")
                .originalUrl("https://blog.com/post/2")
                .thumbnailUrl("https://img.com/2.jpg")
                .publishedAt(LocalDateTime.now())
                .build();

        ArgumentCaptor<ContentDocument> captor = ArgumentCaptor.forClass(ContentDocument.class);

        // when
        contentService.saveContent(dto1);
        contentService.saveContent(dto2);

        // then
        verify(contentDocumentRepository, times(2)).save(captor.capture());
        List<ContentDocument> savedDocs = captor.getAllValues();
        assertThat(savedDocs.get(0).getId()).isNotEqualTo(savedDocs.get(1).getId());
    }
}
