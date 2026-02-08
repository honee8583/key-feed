package com.leedahun.identityservice.domain.keyword.repository;

import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.keyword.entity.Keyword;
import java.util.Set;

import com.leedahun.identityservice.domain.source.entity.Source;
import com.leedahun.identityservice.domain.source.entity.UserSource;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class KeywordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KeywordRepository keywordRepository;

    private User user1;
    private User user2;
    private Keyword keyword1ByUser1; // user1의 키워드 1
    private Keyword keyword2ByUser1; // user1의 키워드 2
    private Keyword keyword2ByUser2; // user2의 키워드 1

    @BeforeEach
    void setUp() {
        user1 = new User();
        user2 = new User();

        user1 = entityManager.persist(user1);
        user2 = entityManager.persist(user2);

        keyword1ByUser1 = Keyword.builder()
                .name("Java")
                .user(user1)
                .build();
        keyword2ByUser1 = Keyword.builder()
                .name("Spring")
                .user(user1)
                .build();
        keyword2ByUser2 = Keyword.builder()
                .name("Docker")
                .user(user2)
                .build();

        entityManager.persist(keyword1ByUser1);
        entityManager.persist(keyword2ByUser1);
        entityManager.persist(keyword2ByUser2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findByUserId: 특정 유저의 모든 키워드 목록 조회")
    void findByUserId_success() {
        // When
        List<Keyword> keywords = keywordRepository.findByUserId(user1.getId());

        // Then
        assertThat(keywords).hasSize(2);
        assertThat(keywords)
                .extracting(Keyword::getName)
                .containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    @DisplayName("findByUserId: 키워드가 없는 유저 조회 시 빈 목록 반환")
    void findByUserId_empty() {
        // Given
        User user3 = entityManager.persist(new User());
        entityManager.flush();

        // When
        List<Keyword> keywords = keywordRepository.findByUserId(user3.getId());

        // Then
        assertThat(keywords).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndUserId: 특정 키워드 ID와 유저 ID로 키워드 조회 성공")
    void findByIdAndUserId_success() {
        // When
        Optional<Keyword> keyword = keywordRepository.findByIdAndUserId(keyword1ByUser1.getId(), user1.getId());

        // Then
        assertThat(keyword).isPresent();
        assertThat(keyword.get().getId()).isEqualTo(keyword1ByUser1.getId());
        assertThat(keyword.get().getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("findByIdAndUserId: 키워드 ID는 맞지만 유저 ID가 틀릴 경우 조회 실패")
    void findByIdAndUserId_wrongUser() {
        // When
        Optional<Keyword> keyword = keywordRepository.findByIdAndUserId(keyword1ByUser1.getId(), user2.getId());

        // Then
        assertThat(keyword).isNotPresent();
    }

    @Test
    @DisplayName("findByIdAndUserId: 존재하지 않는 키워드 ID일 경우 조회 실패")
    void findByIdAndUserId_wrongKeywordId() {
        // When
        Optional<Keyword> keyword = keywordRepository.findByIdAndUserId(999L, user1.getId());

        // Then
        assertThat(keyword).isNotPresent();
    }

    @Test
    @DisplayName("existsByNameAndUser: 이름과 User 객체로 키워드 존재 여부 확인 (True)")
    void existsByNameAndUser_true() {
        // When
        User savedUser1 = entityManager.find(User.class, user1.getId());
        boolean exists = keywordRepository.existsByNameAndUser("Java", savedUser1);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByNameAndUser: 이름이 틀릴 경우 (False)")
    void existsByNameAndUser_false_wrongName() {
        // When
        User savedUser1 = entityManager.find(User.class, user1.getId());
        boolean exists = keywordRepository.existsByNameAndUser("Python", savedUser1);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByNameAndUser: 유저가 틀릴 경우 (False)")
    void existsByNameAndUser_false_wrongUser() {
        // When
        User savedUser2 = entityManager.find(User.class, user2.getId());
        boolean exists = keywordRepository.existsByNameAndUser("Java", savedUser2);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("countByUserId: 특정 유저의 키워드 개수 조회")
    void countByUserId_success() {
        // When
        Long count = keywordRepository.countByUserId(user1.getId());

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("countByUserId: 키워드가 없는 유저의 개수 조회 (0)")
    void countByUserId_zero() {
        // Given
        Long count = keywordRepository.countByUserId(user2.getId());
        assertThat(count).isEqualTo(1L);

        User user3 = entityManager.persist(new User());
        entityManager.flush();

        // When
        Long zeroCount = keywordRepository.countByUserId(user3.getId());

        // Then
        assertThat(zeroCount).isEqualTo(0L);
    }

    @Test
    @DisplayName("findUserIdsByNamesAndSourceId: 키워드와 Source ID로 유저 ID 조회 (중복 제거 확인)")
    void findUserIdsByNamesAndSourceId_test() {
        // given
        Source source1 = Source.builder()
                .url("source1")
                .build();
        Source source2 = Source.builder()
                .url("source2")
                .build();
        entityManager.persist(source1);
        entityManager.persist(source2);

        UserSource userSource1 = UserSource.builder()
                .user(user1)
                .userDefinedName("source1 name")
                .source(source1)
                .build();

        UserSource userSource2 = UserSource.builder()
                .user(user2)
                .userDefinedName("source2 name")
                .source(source2)
                .build();

        entityManager.persist(userSource1);
        entityManager.persist(userSource2);

        entityManager.flush();
        entityManager.clear();

        // when
        Set<String> searchKeywords = Set.of("Java", "Spring", "Docker");
        Long targetSourceId = source1.getId();

        List<Long> resultUserIds = keywordRepository.findUserIdsByNamesAndSourceId(searchKeywords, targetSourceId);

        // Then
        assertThat(resultUserIds).hasSize(1);
        assertThat(resultUserIds).containsExactly(user1.getId());
    }

    @Test
    @DisplayName("findTrendingKeywords: 사용자 등록 수 기준으로 인기 키워드 조회")
    void findTrendingKeywords_success() {
        // Given - user2에게도 "Java" 키워드 추가 (Java: 2명, Spring: 1명, Docker: 1명)
        User savedUser2 = entityManager.find(User.class, user2.getId());
        entityManager.persist(Keyword.builder().name("Java").user(savedUser2).build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<TrendingKeywordProjection> result = keywordRepository.findTrendingKeywords(PageRequest.of(0, 10));

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        assertThat(result.get(0).getUserCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findTrendingKeywords: Pageable로 상위 N개만 조회")
    void findTrendingKeywords_withPageable() {
        // Given - user2에게도 "Java" 키워드 추가
        User savedUser2 = entityManager.find(User.class, user2.getId());
        entityManager.persist(Keyword.builder().name("Java").user(savedUser2).build());
        entityManager.flush();
        entityManager.clear();

        // When - 상위 2개만 조회
        List<TrendingKeywordProjection> result = keywordRepository.findTrendingKeywords(PageRequest.of(0, 2));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        assertThat(result.get(0).getUserCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findTrendingKeywords: 키워드가 없는 경우 빈 목록 반환")
    void findTrendingKeywords_empty() {
        // Given - 모든 키워드 삭제
        keywordRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // When
        List<TrendingKeywordProjection> result = keywordRepository.findTrendingKeywords(PageRequest.of(0, 10));

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findTrendingKeywords: 동일 사용자가 동일 키워드를 중복 등록해도 1명으로 카운트")
    void findTrendingKeywords_distinctUserCount() {
        // Given - user1이 이미 "Java"를 가지고 있는 상태에서 "Java"를 한 번 더 등록
        // 비즈니스 로직상 중복 등록은 막히지만, DISTINCT 쿼리 자체의 정확성을 검증
        // 기존: Java(user1), Spring(user1), Docker(user2)
        // 추가: Java(user1) -> DB에는 Java 2행이지만 DISTINCT user.id로 여전히 1명
        User savedUser1 = entityManager.find(User.class, user1.getId());
        entityManager.persist(Keyword.builder().name("Java").user(savedUser1).build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<TrendingKeywordProjection> result = keywordRepository.findTrendingKeywords(PageRequest.of(0, 10));

        // Then - Java는 user1이 2행 가지고 있지만 DISTINCT로 1명만 카운트
        assertThat(result).hasSize(3);
        assertThat(result).allSatisfy(r -> assertThat(r.getUserCount()).isEqualTo(1L));
    }

    @Test
    @DisplayName("findTrendingKeywords: 여러 사용자가 동일 키워드를 등록한 경우 정확한 카운트")
    void findTrendingKeywords_multipleUsersWithSameKeyword() {
        // Given - 3명의 사용자가 모두 "Java" 등록
        User user3 = entityManager.persist(new User());
        User savedUser2 = entityManager.find(User.class, user2.getId());
        entityManager.persist(Keyword.builder().name("Java").user(savedUser2).build());
        entityManager.persist(Keyword.builder().name("Java").user(user3).build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<TrendingKeywordProjection> result = keywordRepository.findTrendingKeywords(PageRequest.of(0, 10));

        // Then
        assertThat(result.get(0).getName()).isEqualTo("Java");
        assertThat(result.get(0).getUserCount()).isEqualTo(3L);
    }

}