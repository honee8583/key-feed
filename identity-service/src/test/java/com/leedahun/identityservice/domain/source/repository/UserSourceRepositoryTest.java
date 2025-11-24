package com.leedahun.identityservice.domain.source.repository;

import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.source.entity.Source;
import com.leedahun.identityservice.domain.source.entity.UserSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserSourceRepositoryTest {

    @Autowired
    private UserSourceRepository userSourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    @DisplayName("사용자 ID로 해당 사용자의 모든 소스 구독 정보를 조회한다")
    void findByUserId_Success() {
        // given
        User user = User.builder()
                .email("user@test.com")
                .password("password")
                .username("testUser")
                .build();
        userRepository.save(user);

        Source source1 = Source.builder().url("https://blog1.com/rss").build();
        Source source2 = Source.builder().url("https://blog2.com/rss").build();
        sourceRepository.save(source1);
        sourceRepository.save(source2);

        UserSource userSource1 = UserSource.builder()
                .user(user)
                .source(source1)
                .userDefinedName("블로그1")
                .build();

        UserSource userSource2 = UserSource.builder()
                .user(user)
                .source(source2)
                .userDefinedName("블로그2")
                .build();

        userSourceRepository.save(userSource1);
        userSourceRepository.save(userSource2);

        // when
        List<UserSource> result = userSourceRepository.findByUserId(user.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("userDefinedName")
                .containsExactlyInAnyOrder("블로그1", "블로그2");
    }

    @Test
    @DisplayName("구독 ID와 사용자 ID가 일치하는 구독 정보를 조회한다 (본인 소유 확인)")
    void findByIdAndUserId_Success() {
        // given
        User user = User.builder()
                .email("owner@test.com")
                .password("password")
                .username("owner")
                .build();
        userRepository.save(user);

        Source source = Source.builder().url("https://my-source.com/feed").build();
        sourceRepository.save(source);

        UserSource userSource = UserSource.builder()
                .user(user)
                .source(source)
                .userDefinedName("내 소스")
                .build();
        userSourceRepository.save(userSource);

        // when
        Optional<UserSource> result = userSourceRepository.findByIdAndUserId(userSource.getId(), user.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserDefinedName()).isEqualTo("내 소스");
    }

    @Test
    @DisplayName("다른 사용자의 ID로 구독 정보를 조회하면 빈 결과를 반환한다")
    void findByIdAndUserId_Fail_WrongUser() {
        // given
        User owner = User.builder().email("owner@test.com").password("pw").username("owner").build();
        User otherUser = User.builder().email("other@test.com").password("pw").username("other").build();
        userRepository.save(owner);
        userRepository.save(otherUser);

        Source source = Source.builder().url("https://source.com").build();
        sourceRepository.save(source);

        UserSource userSource = UserSource.builder()
                .user(owner)
                .source(source)
                .userDefinedName("Owner's Source")
                .build();
        userSourceRepository.save(userSource);

        // when (구독 ID는 존재하지만, 요청한 User ID가 다름)
        Optional<UserSource> result = userSourceRepository.findByIdAndUserId(userSource.getId(), otherUser.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("특정 사용자가 특정 소스를 이미 구독했는지 확인한다 (중복 검사)")
    void existsByUserIdAndSourceId_True() {
        // given
        User user = User.builder().email("check@test.com").password("pw").username("check").build();
        userRepository.save(user);

        Source source = Source.builder().url("https://duplicate.com").build();
        sourceRepository.save(source);

        UserSource userSource = UserSource.builder().user(user).source(source).userDefinedName("Dup").build();
        userSourceRepository.save(userSource);

        // when
        boolean exists = userSourceRepository.existsByUserIdAndSourceId(user.getId(), source.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("구독하지 않은 소스에 대해서는 false를 반환한다")
    void existsByUserIdAndSourceId_False() {
        // given
        User user = User.builder().email("check@test.com").password("pw").username("check").build();
        userRepository.save(user);

        Source source = Source.builder().url("https://new.com").build();
        sourceRepository.save(source);

        // when
        boolean exists = userSourceRepository.existsByUserIdAndSourceId(user.getId(), source.getId());

        // then
        assertThat(exists).isFalse();
    }
}