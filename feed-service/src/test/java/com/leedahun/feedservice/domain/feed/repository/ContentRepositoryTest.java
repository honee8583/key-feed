package com.leedahun.feedservice.domain.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.leedahun.feedservice.domain.feed.entity.Content;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class ContentRepositoryTest {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("REGEXP를 이용해 제목이나 요약에 키워드가 포함된 게시물을 검색한다")
    void searchByKeywordsKeyset_FirstPage() {
        // given
        createAndPersistContent("Java Spring Boot", "백엔드 개발");
        createAndPersistContent("Python Django", "웹 개발 기초");
        createAndPersistContent("Spring Cloud", "MSA 아키텍처");
        createAndPersistContent("Node.js", "자바스크립트 런타임");
        createAndPersistContent("Java Performance", "GC 튜닝 가이드");

        // 검색 패턴: 'Spring' 또는 'Java'가 포함된 것
        String pattern = "Spring|Java";
        int limit = 2;

        // when
        List<Content> result = contentRepository.searchByKeywordsKeyset(pattern, null, limit);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Performance");
        assertThat(result.get(1).getTitle()).isEqualTo("Spring Cloud");
    }

    @Test
    @DisplayName("커서(마지막 ID) 이후의 데이터를 검색한다")
    void searchByKeywordsKeyset_NextPage() {
        // given
        Content content1 = createAndPersistContent("Java Spring Boot", "백엔드 개발");
        Content content2 = createAndPersistContent("Spring Cloud", "MSA 아키텍처");
        Content content3 = createAndPersistContent("Java Performance", "GC 튜닝 가이드");

        System.out.println(content1.getId());
        System.out.println(content2.getId());
        System.out.println(content3.getId());

        String pattern = "Java";
        int limit = 2;
        Long cursorId = content3.getId();

        // when
        List<Content> result = contentRepository.searchByKeywordsKeyset(pattern, cursorId, limit);

        // then
        // content1을 조회(Java가 들어가고 content3보다 작은 id의 content를 조회)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Spring Boot");
    }

    @Test
    @DisplayName("검색 결과가 없을 경우 빈 리스트를 반환한다")
    void searchByKeywordsKeyset_NoResult() {
        // given
        createAndPersistContent("Docker", "컨테이너");
        createAndPersistContent("Kubernetes", "오케스트레이션");

        // when
        List<Content> result = contentRepository.searchByKeywordsKeyset("Redis", null, 10);

        // then
        assertThat(result).isEmpty();
    }

    private Content createAndPersistContent(String title, String summary) {
        Content content = Content.builder()
                .sourceId(1L)
                .title(title)
                .summary(summary)
                .publishedAt(LocalDateTime.now())
                .build();

        return entityManager.persist(content);
    }
}