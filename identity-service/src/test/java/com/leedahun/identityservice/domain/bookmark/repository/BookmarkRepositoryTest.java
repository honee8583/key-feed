package com.leedahun.identityservice.domain.bookmark.repository;

import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.bookmark.entity.Bookmark;
import com.leedahun.identityservice.domain.bookmark.entity.BookmarkFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private BookmarkFolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private BookmarkFolder folder;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 및 폴더 생성
        user = userRepository.save(User.builder().email("test@test.com").build());
        folder = folderRepository.save(BookmarkFolder.builder().user(user).name("Dev Folder").build());
    }

    @Nested
    @DisplayName("전체 북마크 조회")
    class FindAllBookmarks {

        @Test
        @DisplayName("첫 페이지 조회: ID 내림차순으로 정렬되어 반환된다")
        void findAllByUserIdOrderByIdDesc() {
            // given
            createBookmark("101", null); // id=1
            createBookmark("102", folder); // id=2
            createBookmark("103", null); // id=3

            Pageable pageable = PageRequest.of(0, 2);

            // when
            List<Bookmark> result = bookmarkRepository.findAllByUserIdOrderByIdDesc(user.getId(), pageable);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getContentId()).isEqualTo("103"); // 가장 최근 것
            assertThat(result.get(1).getContentId()).isEqualTo("102");

            // Fetch Join 확인: 폴더가 있는 객체의 폴더 정보가 로딩되었는지
            assertThat(result.get(1).getBookmarkFolder()).isNotNull();
            assertThat(result.get(1).getBookmarkFolder().getName()).isEqualTo("Dev Folder");
        }

        @Test
        @DisplayName("다음 페이지 조회: Cursor(lastId)보다 작은 ID만 반환된다")
        void findAllByUserIdAndIdLessThanOrderByIdDesc() {
            // given
            Bookmark b1 = createBookmark("101", null); // 가장 먼저 생성 (ID 작음)
            Bookmark b2 = createBookmark("102", folder);
            Bookmark b3 = createBookmark("103", null); // 가장 나중에 생성 (ID 큼)

            // lastId를 b3의 ID로 설정 (b3보다 작은 것들을 조회)
            Long lastId = b3.getId();
            Pageable pageable = PageRequest.of(0, 10);

            // when
            List<Bookmark> result = bookmarkRepository.findAllByUserIdAndIdLessThanOrderByIdDesc(
                    user.getId(), lastId, pageable
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("id").containsExactly(b2.getId(), b1.getId());
        }
    }

    @Nested
    @DisplayName("특정 폴더 북마크 조회")
    class FindFolderBookmarks {

        @Test
        @DisplayName("특정 폴더에 속한 북마크만 조회된다")
        void findInFolder() {
            // given
            createBookmark("101", null); // 미분류
            createBookmark("102", folder); // 대상 폴더
            createBookmark("103", folder); // 대상 폴더

            // 다른 폴더 생성
            BookmarkFolder otherFolder = folderRepository.save(BookmarkFolder.builder().user(user).name("Other").build());
            createBookmark("104", otherFolder);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            List<Bookmark> result = bookmarkRepository.findAllByUserIdAndBookmarkFolderIdOrderByIdDesc(
                    user.getId(), folder.getId(), pageable
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getContentId()).isEqualTo("103");
            assertThat(result.get(1).getContentId()).isEqualTo("102");
        }

        @Test
        @DisplayName("특정 폴더 다음 페이지 조회 (Cursor)")
        void findInFolderNextPage() {
            // given
            Bookmark b1 = createBookmark("101", folder);
            Bookmark b2 = createBookmark("102", folder);

            Long lastId = b2.getId();
            Pageable pageable = PageRequest.of(0, 10);

            // when
            List<Bookmark> result = bookmarkRepository.findAllByUserIdAndBookmarkFolderIdAndIdLessThanOrderByIdDesc(
                    user.getId(), folder.getId(), lastId, pageable
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(b1.getId());
        }
    }

    @Nested
    @DisplayName("미분류 북마크 조회")
    class FindUncategorizedBookmarks {

        @Test
        @DisplayName("폴더가 없는(null) 북마크만 조회된다")
        void findUncategorized() {
            // given
            createBookmark("101", folder); // 폴더 있음
            createBookmark("102", null);   // 미분류
            createBookmark("103", null);   // 미분류

            Pageable pageable = PageRequest.of(0, 10);

            // when
            List<Bookmark> result = bookmarkRepository.findAllByUserIdAndBookmarkFolderIsNullOrderByIdDesc(
                    user.getId(), pageable
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getContentId()).isEqualTo("103");
            assertThat(result.get(1).getContentId()).isEqualTo("102");
            assertThat(result.get(0).getBookmarkFolder()).isNull();
        }

        @Test
        @DisplayName("미분류 북마크 다음 페이지 조회")
        void findUncategorizedNextPage() {
            // given
            Bookmark b1 = createBookmark("101", null);
            Bookmark b2 = createBookmark("102", null);

            Long lastId = b2.getId();
            Pageable pageable = PageRequest.of(0, 10);

            // when
            List<Bookmark> result = bookmarkRepository.findAllByUserIdAndBookmarkFolderIsNullAndIdLessThanOrderByIdDesc(
                    user.getId(), lastId, pageable
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(b1.getId());
        }
    }

    @Nested
    @DisplayName("유효성 및 존재 확인")
    class ValidationCheck {

        @Test
        @DisplayName("이미 저장된 콘텐츠 ID라면 true를 반환한다")
        void existsByUserIdAndContentId() {
            // given
            createBookmark("999", null);

            // when
            boolean exists = bookmarkRepository.existsByUserIdAndContentId(user.getId(), "999");
            boolean notExists = bookmarkRepository.existsByUserIdAndContentId(user.getId(), "888");

            // then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }

        @Test
        @DisplayName("내 북마크 조회 성공 (findByIdAndUserId)")
        void findByIdAndUserId_success() {
            // given
            Bookmark bookmark = createBookmark("101", null);

            // when
            Optional<Bookmark> result = bookmarkRepository.findByIdAndUserId(bookmark.getId(), user.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(bookmark);
        }

        @Test
        @DisplayName("다른 사람의 북마크 조회 시 Empty 반환")
        void findByIdAndUserId_fail_otherUser() {
            // given
            User otherUser = userRepository.save(User.builder().email("other@test.com").build());
            Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
                    .user(otherUser)
                    .contentId("101")
                    .build());

            // when
            Optional<Bookmark> result = bookmarkRepository.findByIdAndUserId(bookmark.getId(), user.getId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("ContentId 목록으로 북마크 조회")
    class FindBookmarksByContentIds {

        @Test
        @DisplayName("요청한 ContentId 목록에 포함된 내 북마크만 반환한다")
        void findAllByUserIdAndContentIdIn() {
            // given
            createBookmark("100", null);
            createBookmark("200", null);
            createBookmark("300", null); // 검색 대상 아님

            // 100, 200은 존재하고 999는 존재하지 않음
            List<String> requestIds = List.of("100", "200", "999");

            // when
            List<Bookmark> result = bookmarkRepository.findAllByUserIdAndContentIdIn(user.getId(), requestIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("contentId")
                    .containsExactlyInAnyOrder("100", "200");
        }

        @Test
        @DisplayName("다른 유저가 같은 ContentId를 북마크 했더라도 내 것만 조회된다")
        void findAllByUserIdAndContentIdIn_ignoreOtherUser() {
            // given
            createBookmark("100", null); // 내 북마크

            // 다른 유저 생성 및 동일한 컨텐츠 북마크
            User otherUser = userRepository.save(User.builder().email("other@test.com").build());
            bookmarkRepository.save(Bookmark.builder()
                    .user(otherUser)
                    .contentId("100")
                    .build());

            List<String> requestIds = List.of("100");

            // when
            List<Bookmark> result = bookmarkRepository.findAllByUserIdAndContentIdIn(user.getId(), requestIds);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("검색할 ContentId 리스트가 비어있으면 빈 결과를 반환한다")
        void findAllByUserIdAndContentIdIn_emptyList() {
            // given
            createBookmark("100", null);
            List<String> emptyList = List.of();

            // when
            List<Bookmark> result = bookmarkRepository.findAllByUserIdAndContentIdIn(user.getId(), emptyList);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("폴더 삭제 시 북마크와 연결 해제")
    class UpdateFolderToNull {

        @Test
        @DisplayName("특정 폴더 ID를 가진 북마크들의 폴더 정보를 모두 null로 변경한다")
        void updateFolderToNull_success() {
            // given
            BookmarkFolder targetFolder = folderRepository.save(
                    BookmarkFolder.builder()
                            .user(user)
                            .name("Delete Target")
                            .build()
            );
            Bookmark b1 = createBookmark("101", targetFolder);
            Bookmark b2 = createBookmark("102", targetFolder);

            BookmarkFolder otherFolder = folderRepository.save(
                    BookmarkFolder.builder()
                            .user(user)
                            .name("Safe Folder")
                            .build()
            );
            Bookmark b3 = createBookmark("103", otherFolder);

            // when
            bookmarkRepository.updateFolderToNull(targetFolder.getId());

            // then
            Bookmark updatedB1 = bookmarkRepository.findById(b1.getId()).orElseThrow();
            Bookmark updatedB2 = bookmarkRepository.findById(b2.getId()).orElseThrow();
            Bookmark updatedB3 = bookmarkRepository.findById(b3.getId()).orElseThrow();

            // 폴더에 있던 북마크들은 폴더가 null로 변해야 함
            assertThat(updatedB1.getBookmarkFolder()).isNull();
            assertThat(updatedB2.getBookmarkFolder()).isNull();

            // 다른 폴더에 있던 북마크는 그대로 유지되어야 함
            assertThat(updatedB3.getBookmarkFolder()).isNotNull();
            assertThat(updatedB3.getBookmarkFolder().getId()).isEqualTo(otherFolder.getId());
        }
    }

    private Bookmark createBookmark(String contentId, BookmarkFolder folder) {
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .contentId(contentId)
                .bookmarkFolder(folder)
                .build();
        return bookmarkRepository.save(bookmark);
    }
}