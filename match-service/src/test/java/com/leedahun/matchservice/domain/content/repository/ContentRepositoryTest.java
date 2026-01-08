package com.leedahun.matchservice.domain.content.repository;

import com.leedahun.matchservice.domain.content.entity.Content;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContentRepositoryTest {

    @Autowired
    private ContentRepository contentRepository;

    @Test
    @DisplayName("SourceId와 OriginalUrl이 모두 일치하는 콘텐츠가 존재하면 true를 반환한다")
    void existsBySourceIdAndOriginalUrl_Exists() {
        // given
        Long sourceId = 1L;
        String originalUrl = "https://test-blog.com/post/1";

        Content content = Content.builder()
                .sourceId(sourceId)
                .title("Test Title")
                .summary("Test Summary")
                .originalUrl(originalUrl)
                .thumbnailUrl("https://thumb.com/img.jpg")
                .publishedAt(LocalDateTime.now())
                .build();
        contentRepository.save(content);

        // when
        boolean exists = contentRepository.existsBySourceIdAndOriginalUrl(sourceId, originalUrl);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("SourceId는 일치하지만 OriginalUrl이 다르면 false를 반환한다")
    void existsBySourceIdAndOriginalUrl_DifferentUrl() {
        // given
        Long sourceId = 1L;
        String savedUrl = "https://test-blog.com/post/1";
        String targetUrl = "https://test-blog.com/post/2"; // 다른 URL

        Content content = Content.builder()
                .sourceId(sourceId)
                .title("Test Title")
                .summary("Test Summary")
                .originalUrl(savedUrl)
                .publishedAt(LocalDateTime.now())
                .build();
        contentRepository.save(content);

        // when
        boolean exists = contentRepository.existsBySourceIdAndOriginalUrl(sourceId, targetUrl);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("OriginalUrl은 일치하지만 SourceId가 다르면 false를 반환한다")
    void existsBySourceIdAndOriginalUrl_DifferentSourceId() {
        // given
        Long savedSourceId = 1L;
        Long targetSourceId = 2L; // 다른 소스 ID
        String url = "https://test-blog.com/post/1";

        Content content = Content.builder()
                .sourceId(savedSourceId)
                .title("Test Title")
                .summary("Test Summary")
                .originalUrl(url)
                .publishedAt(LocalDateTime.now())
                .build();
        contentRepository.save(content);

        // when
        boolean exists = contentRepository.existsBySourceIdAndOriginalUrl(targetSourceId, url);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("데이터가 하나도 없을 때는 false를 반환한다")
    void existsBySourceIdAndOriginalUrl_Empty() {
        // given

        // when
        boolean exists = contentRepository.existsBySourceIdAndOriginalUrl(1L, "https://any.com");

        // then
        assertThat(exists).isFalse();
    }

}