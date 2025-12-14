package com.leedahun.notificationservice.domain.notification.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SseEmitterRepositoryTest {

    private SseEmitterRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SseEmitterRepository();
    }

    @Test
    @DisplayName("Emitter를 저장하고, 저장된 객체가 반환되는지 확인한다.")
    void save() {
        // given
        String emitterId = "1_1710000000000";
        SseEmitter emitter = new SseEmitter();

        // when
        SseEmitter savedEmitter = repository.save(emitterId, emitter);

        // then
        assertThat(savedEmitter).isEqualTo(emitter);
        // 저장이 잘 되었는지 조회를 통해 재확인
        Map<String, SseEmitter> result = repository.findAllEmitterStartWithByUserId("1");
        assertThat(result).containsEntry(emitterId, emitter);
    }

    @Test
    @DisplayName("특정 사용자(UserId)의 모든 Emitter를 조회한다.")
    void findAllEmitterStartWithByUserId() {
        // given
        String userId = "1";

        // Target User의 Emitter 2개 생성
        String id1 = userId + "_" + System.currentTimeMillis();
        String id2 = userId + "_" + (System.currentTimeMillis() + 100);
        repository.save(id1, new SseEmitter());
        repository.save(id2, new SseEmitter());

        // 다른 사용자의 Emitter 1개 생성 (조회되면 안 됨)
        String otherUserId = "2";
        String id3 = otherUserId + "_" + System.currentTimeMillis();
        repository.save(id3, new SseEmitter());

        // when
        Map<String, SseEmitter> result = repository.findAllEmitterStartWithByUserId(userId);

        // then
        assertThat(result).hasSize(2); // 내 것 2개만 있어야 함
        assertThat(result).containsKey(id1);
        assertThat(result).containsKey(id2);
        assertThat(result).doesNotContainKey(id3); // 다른 사람 것은 없어야 함
    }

    @Test
    @DisplayName("사용자 ID가 접두사로 일치하지 않으면 조회되지 않아야 한다. (예: user '1' 검색 시 '10'은 제외)")
    void findAllEmitter_PrefixCheck() {
        // given
        String targetUserId = "1";
        String confusingUserId = "10"; // '1'로 시작하지만 '1_' 패턴은 아님

        String id1 = targetUserId + "_time1";
        String id2 = confusingUserId + "_time2";

        repository.save(id1, new SseEmitter());
        repository.save(id2, new SseEmitter());

        // when
        Map<String, SseEmitter> result = repository.findAllEmitterStartWithByUserId(targetUserId);

        // then
        // 코드 로직이 startsWith(userId + "_") 이므로 '1_'만 찾아야 함
        assertThat(result).hasSize(1);
        assertThat(result).containsKey(id1);
        assertThat(result).doesNotContainKey(id2);
    }

    @Test
    @DisplayName("ID로 Emitter를 삭제한다.")
    void deleteById() {
        // given
        String emitterId = "1_12345";
        SseEmitter emitter = new SseEmitter();
        repository.save(emitterId, emitter);

        // 삭제 전 확인
        assertThat(repository.findAllEmitterStartWithByUserId("1")).hasSize(1);

        // when
        repository.deleteById(emitterId);

        // then
        Map<String, SseEmitter> result = repository.findAllEmitterStartWithByUserId("1");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID를 삭제해도 에러가 발생하지 않는다.")
    void deleteById_NonExistent() {
        // given
        String nonExistentId = "999_9999";

        // when & then
        assertDoesNotThrow(() -> repository.deleteById(nonExistentId));
    }
}