package com.leedahun.identityservice.domain.bookmark.repository;

import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.bookmark.entity.BookmarkFolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookmarkFolderRepositoryTest {

    @Autowired
    private BookmarkFolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 ID로 폴더 목록 조회 시 ID 순으로 정렬되어 반환된다")
    void findAllByUserIdOrderById() {
        // given
        User user = userRepository.save(User.builder().email("test@test.com").build());

        BookmarkFolder folder1 = BookmarkFolder.builder().user(user).name("Folder 1").build();
        BookmarkFolder folder2 = BookmarkFolder.builder().user(user).name("Folder 2").build();

        folderRepository.save(folder1);
        folderRepository.save(folder2);

        // when
        List<BookmarkFolder> result = folderRepository.findAllByUserIdOrderById(user.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Folder 1");
        assertThat(result.get(1).getName()).isEqualTo("Folder 2");
        assertThat(result.get(0).getId()).isLessThan(result.get(1).getId());
    }

    @Test
    @DisplayName("특정 사용자의 폴더 이름 중복 여부를 확인한다 - 중복인 경우")
    void existsByUserIdAndName_true() {
        // given
        User user = userRepository.save(User.builder().email("test@test.com").build());
        String folderName = "My Folder";

        folderRepository.save(BookmarkFolder.builder().user(user).name(folderName).build());

        // when
        boolean exists = folderRepository.existsByUserIdAndName(user.getId(), folderName);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 사용자의 폴더 이름 중복 여부를 확인한다 - 중복이 아닌 경우")
    void existsByUserIdAndName_false() {
        // given
        User user = userRepository.save(User.builder().email("test@test.com").build());
        folderRepository.save(BookmarkFolder.builder().user(user).name("Existing").build());

        // when
        boolean exists = folderRepository.existsByUserIdAndName(user.getId(), "New Folder");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("다른 사용자의 폴더 이름과는 중복되어도 false를 반환한다")
    void existsByUserIdAndName_otherUser() {
        // given
        User user1 = userRepository.save(User.builder().email("user1@test.com").build());
        User user2 = userRepository.save(User.builder().email("user2@test.com").build());
        String folderName = "Common Name";

        // User1이 해당 이름의 폴더를 가짐
        folderRepository.save(BookmarkFolder.builder().user(user1).name(folderName).build());

        // when: User2가 같은 이름으로 체크
        boolean exists = folderRepository.existsByUserIdAndName(user2.getId(), folderName);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("사용자의 북마크 폴더 개수를 정확히 반환한다")
    void countByUserId() {
        // given
        User user = userRepository.save(User.builder().email("count@test.com").build());

        folderRepository.save(BookmarkFolder.builder().user(user).name("F1").build());
        folderRepository.save(BookmarkFolder.builder().user(user).name("F2").build());
        folderRepository.save(BookmarkFolder.builder().user(user).name("F3").build());

        // when
        long count = folderRepository.countByUserId(user.getId());

        // then
        assertThat(count).isEqualTo(3);
    }
}