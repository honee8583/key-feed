package com.leedahun.identityservice.domain.source.repository;

import com.leedahun.identityservice.domain.source.entity.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SourceRepositoryTest {

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    @DisplayName("URL로 소스를 조회한다")
    void findByUrl_Success() {
        // given
        String url = "https://techblog.woowahan.com/feed";
        Source source = Source.builder()
                .url(url)
                .build();

        sourceRepository.save(source);

        // when
        Optional<Source> result = sourceRepository.findByUrl(url);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUrl()).isEqualTo(url);
        assertThat(result.get().getId()).isNotNull(); // 저장 후 ID 생성 확인
    }

    @Test
    @DisplayName("존재하지 않는 URL로 조회 시 빈 Optional을 반환한다")
    void findByUrl_NotFound() {
        // given
        String url = "https://unknown-url.com/rss";

        // when
        Optional<Source> result = sourceRepository.findByUrl(url);

        // then
        assertThat(result).isEmpty();
    }
}