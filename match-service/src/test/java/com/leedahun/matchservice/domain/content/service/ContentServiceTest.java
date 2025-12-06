package com.leedahun.matchservice.domain.content.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.leedahun.matchservice.domain.content.document.ContentDocument;
import com.leedahun.matchservice.domain.content.repository.ContentDocumentRepository;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @InjectMocks
    private ContentService contentService;

    @Mock
    private ContentDocumentRepository contentDocumentRepository;

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

        // when
        contentService.saveContent(dto);

        // then
        // save 메서드가 1번 호출되었는지 확인 (저장 성공)
        verify(contentDocumentRepository, times(1)).save(any(ContentDocument.class));
    }

}