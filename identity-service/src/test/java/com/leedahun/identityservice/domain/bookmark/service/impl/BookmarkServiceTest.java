package com.leedahun.identityservice.domain.bookmark.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leedahun.identityservice.common.error.exception.EntityAlreadyExistsException;
import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkFolderRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkFolderResponseDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkRequestDto;
import com.leedahun.identityservice.domain.bookmark.entity.Bookmark;
import com.leedahun.identityservice.domain.bookmark.entity.BookmarkFolder;
import com.leedahun.identityservice.domain.bookmark.exception.FolderAccessDeniedException;
import com.leedahun.identityservice.domain.bookmark.exception.FolderLimitExceededException;
import com.leedahun.identityservice.domain.bookmark.repository.BookmarkFolderRepository;
import com.leedahun.identityservice.domain.bookmark.repository.BookmarkRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.leedahun.identityservice.infra.client.FeedInternalApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceImplTest {

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private BookmarkFolderRepository folderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FeedInternalApiClient feedInternalApiClient;

    private final int FOLDER_MAX_COUNT = 5;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookmarkService, "folderMaxCount", FOLDER_MAX_COUNT);
    }

    @Nested
    @DisplayName("폴더 생성 (createFolder)")
    class CreateFolder {

        @Test
        @DisplayName("성공: 폴더 이름 중복 없고 개수 제한 안 넘으면 생성 성공")
        void createFolder_success() {
            // given
            Long userId = 1L;
            BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                    .name("Dev")
                    .build();
            User user = User.builder().id(userId).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(folderRepository.existsByUserIdAndName(userId, "Dev")).thenReturn(false);
            when(folderRepository.countByUserId(userId)).thenReturn(0L);

            // save 호출 시 ID가 세팅된 객체 반환 시뮬레이션
            when(folderRepository.save(any(BookmarkFolder.class))).thenAnswer(invocation -> {
                BookmarkFolder folder = invocation.getArgument(0);
                ReflectionTestUtils.setField(folder, "id", 100L);
                return folder;
            });

            // when
            Long folderId = bookmarkService.createFolder(userId, request);

            // then
            assertThat(folderId).isEqualTo(100L);
            verify(folderRepository, times(1)).save(any(BookmarkFolder.class));
        }

        @Test
        @DisplayName("실패: 폴더 이름 중복")
        void createFolder_fail_duplicateName() {
            // given
            Long userId = 1L;
            BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                    .name("Dev")
                    .build();
            User user = User.builder().id(userId).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(folderRepository.existsByUserIdAndName(userId, "Dev")).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> bookmarkService.createFolder(userId, request))
                    .isInstanceOf(EntityAlreadyExistsException.class);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 유저로 요청 시 예외 발생")
        void getUser_fail_notFound() {
            // given
            Long userId = 999L;
            BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                    .name("Test Folder")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookmarkService.createFolder(userId, request))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패: 폴더 생성 개수 초과")
        void createFolder_fail_limitExceeded() {
            // given
            Long userId = 1L;
            BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                    .name("Dev")
                    .build();
            User user = User.builder().id(userId).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(folderRepository.existsByUserIdAndName(userId, "Dev")).thenReturn(false);
            when(folderRepository.countByUserId(userId)).thenReturn((long) FOLDER_MAX_COUNT);

            // when & then
            assertThatThrownBy(() -> bookmarkService.createFolder(userId, request))
                    .isInstanceOf(FolderLimitExceededException.class);
        }
    }

    @Nested
    @DisplayName("폴더 수정 (updateFolder)")
    class UpdateFolder {

        @Test
        @DisplayName("성공: 폴더 정보 수정")
        void updateFolder_success() {
            // given
            Long userId = 1L;
            Long folderId = 10L;
            User user = User.builder().id(userId).build();
            BookmarkFolder folder = BookmarkFolder.builder()
                    .id(folderId)
                    .user(user)
                    .name("OldName")
                    .icon("old-icon")
                    .color("old-color")
                    .build();

            BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                    .name("NewName")
                    .icon("new-icon")
                    .color("new-color")
                    .build();

            when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));
            when(folderRepository.existsByUserIdAndName(userId, "NewName")).thenReturn(false);

            // when
            bookmarkService.updateFolder(userId, folderId, request);

            // then
            assertThat(folder.getName()).isEqualTo("NewName");
            assertThat(folder.getIcon()).isEqualTo("new-icon");
            assertThat(folder.getColor()).isEqualTo("new-color");
        }

        @Test
        @DisplayName("성공: 폴더 이름은 그대로 유지하고 아이콘/색상만 변경")
        void updateFolder_success_sameNameDifferentAttributes() {
            // given
            Long userId = 1L;
            Long folderId = 10L;
            User user = User.builder().id(userId).build();
            BookmarkFolder folder = BookmarkFolder.builder()
                    .id(folderId)
                    .user(user)
                    .name("SameName")
                    .icon("old-icon")
                    .color("old-color")
                    .build();

            BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                    .name("SameName")  // 동일한 이름
                    .icon("new-icon")
                    .color("new-color")
                    .build();

            when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));
            // 폴더명이 같으므로 중복 체크 로직이 실행되지 않음

            // when
            bookmarkService.updateFolder(userId, folderId, request);

            // then
            assertThat(folder.getName()).isEqualTo("SameName");
            assertThat(folder.getIcon()).isEqualTo("new-icon");
            assertThat(folder.getColor()).isEqualTo("new-color");
            verify(folderRepository, never()).existsByUserIdAndName(anyLong(), anyString());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 폴더")
        void updateFolder_fail_folderNotFound() {
            // given
            Long userId = 1L;
            Long invalidFolderId = 999L;
            BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                    .name("NewName")
                    .build();

            when(folderRepository.findById(invalidFolderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookmarkService.updateFolder(userId, invalidFolderId, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("BookmarkFolder");
        }

        @Test
        @DisplayName("실패: 다른 사용자의 폴더 수정 시도")
        void updateFolder_fail_accessDenied() {
            // given
            Long userId = 1L;
            Long otherUserId = 2L;
            Long folderId = 10L;

            User otherUser = User.builder().id(otherUserId).build();
            BookmarkFolder folder = BookmarkFolder.builder()
                    .id(folderId)
                    .user(otherUser)
                    .name("OldName")
                    .build();

            BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                    .name("NewName")
                    .build();

            when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

            // when & then
            assertThatThrownBy(() -> bookmarkService.updateFolder(userId, folderId, request))
                    .isInstanceOf(FolderAccessDeniedException.class);
        }

        @Test
        @DisplayName("실패: 변경하려는 폴더 이름이 이미 존재")
        void updateFolder_fail_duplicateName() {
            // given
            Long userId = 1L;
            Long folderId = 10L;
            User user = User.builder().id(userId).build();
            BookmarkFolder folder = BookmarkFolder.builder()
                    .id(folderId)
                    .user(user)
                    .name("OldName")
                    .build();

            BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                    .name("DuplicateName")
                    .build();

            when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));
            when(folderRepository.existsByUserIdAndName(userId, "DuplicateName")).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> bookmarkService.updateFolder(userId, folderId, request))
                    .isInstanceOf(EntityAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("폴더 삭제 (deleteFolder)")
    class DeleteFolder {

        @Test
        @DisplayName("성공: 폴더 내부의 북마크를 미분류로 변경 후 폴더 삭제")
        void deleteFolder_success() {
            // given
            Long userId = 1L;
            Long folderId = 10L;
            User user = User.builder().id(userId).build();
            BookmarkFolder folder = BookmarkFolder.builder()
                    .id(folderId)
                    .user(user)
                    .build();

            when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

            // when
            bookmarkService.deleteFolder(userId, folderId);

            // then
            verify(bookmarkRepository).updateFolderToNull(folderId);
            verify(folderRepository).delete(folder);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 폴더 삭제 시도")
        void deleteFolder_fail_notFound() {
            // given
            Long userId = 1L;
            Long invalidFolderId = 999L;

            when(folderRepository.findById(invalidFolderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookmarkService.deleteFolder(userId, invalidFolderId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("BookmarkFolder");

            verify(bookmarkRepository, never()).updateFolderToNull(anyLong());
            verify(folderRepository, never()).delete(any(BookmarkFolder.class));
        }

        @Test
        @DisplayName("실패: 다른 사람의 폴더 삭제 시도 (접근 거부)")
        void deleteFolder_fail_accessDenied() {
            // given
            Long userId = 1L;
            Long otherUserId = 2L;
            Long folderId = 10L;

            User otherUser = User.builder().id(otherUserId).build();
            BookmarkFolder folder = BookmarkFolder.builder()
                    .id(folderId)
                    .user(otherUser) // 소유자가 다름
                    .build();

            when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

            // when & then
            assertThatThrownBy(() -> bookmarkService.deleteFolder(userId, folderId))
                    .isInstanceOf(FolderAccessDeniedException.class);

            verify(bookmarkRepository, never()).updateFolderToNull(anyLong());
            verify(folderRepository, never()).delete(any(BookmarkFolder.class));
        }
    }

    @Nested
    @DisplayName("북마크 추가 (addBookmark)")
    class AddBookmark {
        @Test
        @DisplayName("성공: 폴더 없이 북마크 추가")
        void addBookmark_success_noFolder() {
            // given
            Long userId = 1L;
            BookmarkRequestDto request = BookmarkRequestDto.builder()
                    .contentId("10")
                    .folderId(null)
                    .build();
            User user = User.builder()
                    .id(userId)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(bookmarkRepository.existsByUserIdAndContentId(userId, "10")).thenReturn(false);

            when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(invocation -> {
                Bookmark bookmark = invocation.getArgument(0);
                ReflectionTestUtils.setField(bookmark, "id", 200L);
                return bookmark;
            });

            // when
            Long bookmarkId = bookmarkService.addBookmark(userId, request);

            // then
            assertThat(bookmarkId).isEqualTo(200L);
            verify(bookmarkRepository).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("성공: 폴더 지정하여 북마크 추가")
        void addBookmark_success_withFolder() {
            // given
            Long userId = 1L;
            Long folderId = 50L;
            BookmarkRequestDto request = new BookmarkRequestDto("10", folderId);
            User user = User.builder().id(userId).build();
            BookmarkFolder folder = BookmarkFolder.builder().id(folderId).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(bookmarkRepository.existsByUserIdAndContentId(userId, "10")).thenReturn(false);
            when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

            when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(invocation -> {
                Bookmark b = invocation.getArgument(0);
                ReflectionTestUtils.setField(b, "id", 201L);
                return b;
            });

            // when
            Long bookmarkId = bookmarkService.addBookmark(userId, request);

            // then
            assertThat(bookmarkId).isEqualTo(201L);
        }

        @Test
        @DisplayName("실패: 이미 등록된 컨텐츠(중복 북마크)")
        void addBookmark_fail_duplicated() {
            // given
            Long userId = 1L;
            BookmarkRequestDto request = new BookmarkRequestDto("10", null);
            User user = User.builder().id(userId).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(bookmarkRepository.existsByUserIdAndContentId(userId, "10")).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> bookmarkService.addBookmark(userId, request))
                    .isInstanceOf(EntityAlreadyExistsException.class);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 폴더로 요청 시 예외 발생")
        void getBookmarkFolder_fail_notFound() {
            // given
            Long userId = 1L;
            Long invalidFolderId = 999L;
            BookmarkRequestDto request = new BookmarkRequestDto("10", invalidFolderId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
            when(folderRepository.findById(invalidFolderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookmarkService.addBookmark(userId, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("BookmarkFolder");
        }
    }

    @Nested
    @DisplayName("북마크 목록 조회 (getBookmarks) - 조건별 분기 테스트")
    class GetBookmarks {

        private final Long userId = 1L;
        private final int size = 10;

        @Test
        @DisplayName("전체 조회 - 첫 페이지 (lastId == null)")
        void getBookmarks_all_firstPage() {
            // given
            Long lastId = null;
            Long folderId = null;

            when(bookmarkRepository.findAllByUserIdOrderByIdDesc(eq(userId), any(Pageable.class)))
                    .thenReturn(List.of());

            // Mock 객체는 별도 설정을 안하면 기본적으로 빈 리스트를 반환하므로 when(...).thenReturn(Collections.emptyList()) 생략 가능
            // 하지만 명시적으로 작성해주면 더 안전합니다.
            when(feedInternalApiClient.getContentsByIds(anyList())).thenReturn(Collections.emptyList());

            // when
            bookmarkService.getBookmarks(userId, lastId, folderId, size);

            // then
            verify(bookmarkRepository).findAllByUserIdOrderByIdDesc(eq(userId), any(Pageable.class));
            verify(feedInternalApiClient).getContentsByIds(anyList());
        }

        @Test
        @DisplayName("전체 조회 - 다음 페이지 (lastId != null)")
        void getBookmarks_all_nextPage() {
            // given
            Long lastId = 50L;
            Long folderId = null;

            when(bookmarkRepository.findAllByUserIdAndIdLessThanOrderByIdDesc(eq(userId), eq(lastId), any(Pageable.class)))
                    .thenReturn(List.of());
            when(feedInternalApiClient.getContentsByIds(anyList())).thenReturn(Collections.emptyList());

            // when
            bookmarkService.getBookmarks(userId, lastId, folderId, size);

            // then
            verify(bookmarkRepository).findAllByUserIdAndIdLessThanOrderByIdDesc(eq(userId), eq(lastId), any(Pageable.class));
        }

        @Test
        @DisplayName("미분류 조회 - 첫 페이지 (lastId == null)")
        void getBookmarks_uncategorized_firstPage() {
            // given
            Long lastId = null;
            Long folderId = 0L;

            when(bookmarkRepository.findAllByUserIdAndBookmarkFolderIsNullOrderByIdDesc(eq(userId), any(Pageable.class)))
                    .thenReturn(List.of());
            when(feedInternalApiClient.getContentsByIds(anyList())).thenReturn(Collections.emptyList());

            // when
            bookmarkService.getBookmarks(userId, lastId, folderId, size);

            // then
            verify(bookmarkRepository).findAllByUserIdAndBookmarkFolderIsNullOrderByIdDesc(eq(userId), any(Pageable.class));
        }

        @Test
        @DisplayName("미분류 조회 - 다음 페이지 (lastId != null)")
        void getBookmarks_uncategorized_nextPage() {
            // given
            Long lastId = 50L;
            Long folderId = 0L;

            when(bookmarkRepository.findAllByUserIdAndBookmarkFolderIsNullAndIdLessThanOrderByIdDesc(eq(userId), eq(lastId), any(Pageable.class)))
                    .thenReturn(List.of());
            when(feedInternalApiClient.getContentsByIds(anyList())).thenReturn(Collections.emptyList());

            // when
            bookmarkService.getBookmarks(userId, lastId, folderId, size);

            // then
            verify(bookmarkRepository).findAllByUserIdAndBookmarkFolderIsNullAndIdLessThanOrderByIdDesc(eq(userId), eq(lastId), any(Pageable.class));
        }

        @Test
        @DisplayName("특정 폴더 조회 - 첫 페이지 (lastId == null)")
        void getBookmarks_inFolder_firstPage() {
            // given
            Long lastId = null;
            Long folderId = 100L;

            when(bookmarkRepository.findAllByUserIdAndBookmarkFolderIdOrderByIdDesc(eq(userId), eq(folderId), any(Pageable.class)))
                    .thenReturn(List.of());
            when(feedInternalApiClient.getContentsByIds(anyList())).thenReturn(Collections.emptyList());

            // when
            bookmarkService.getBookmarks(userId, lastId, folderId, size);

            // then
            verify(bookmarkRepository).findAllByUserIdAndBookmarkFolderIdOrderByIdDesc(eq(userId), eq(folderId), any(Pageable.class));
        }

        @Test
        @DisplayName("특정 폴더 조회 - 다음 페이지 (lastId != null)")
        void getBookmarks_inFolder_nextPage() {
            // given
            Long lastId = 50L;
            Long folderId = 100L;

            when(bookmarkRepository.findAllByUserIdAndBookmarkFolderIdAndIdLessThanOrderByIdDesc(eq(userId), eq(folderId), eq(lastId), any(Pageable.class)))
                    .thenReturn(List.of());
            when(feedInternalApiClient.getContentsByIds(anyList())).thenReturn(Collections.emptyList());

            // when
            bookmarkService.getBookmarks(userId, lastId, folderId, size);

            // then
            verify(bookmarkRepository).findAllByUserIdAndBookmarkFolderIdAndIdLessThanOrderByIdDesc(eq(userId), eq(folderId), eq(lastId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("북마크 삭제 및 이동")
    class ModifyBookmark {

        @Test
        @DisplayName("북마크 삭제 성공")
        void deleteBookmark_success() {
            // given
            Long userId = 1L;
            Long bookmarkId = 10L;
            Bookmark bookmark = Bookmark.builder().id(bookmarkId).build();

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, userId)).thenReturn(Optional.of(bookmark));

            // when
            bookmarkService.deleteBookmark(userId, bookmarkId);

            // then
            verify(bookmarkRepository).delete(bookmark);
        }

        @Test
        @DisplayName("북마크 이동 성공")
        void moveBookmark_success() {
            // given
            Long userId = 1L;
            Long bookmarkId = 10L;
            Long targetFolderId = 50L;

            User user = User.builder().id(userId).build();
            Bookmark bookmark = Bookmark.builder().id(bookmarkId).user(user).build();
            BookmarkFolder folder = BookmarkFolder.builder().id(targetFolderId).user(user).build();

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, userId)).thenReturn(Optional.of(bookmark));
            when(folderRepository.findById(targetFolderId)).thenReturn(Optional.of(folder));

            // when
            bookmarkService.moveBookmark(userId, bookmarkId, targetFolderId);

            // then
            assertThat(bookmark.getBookmarkFolder()).isEqualTo(folder);
        }

        @Test
        @DisplayName("북마크 삭제 실패: 존재하지 않거나 소유권이 없는 북마크 요청 시 예외 발생")
        void getBookmarkOwnedByUser_fail_notFound() {
            // given
            Long userId = 1L;
            Long invalidBookmarkId = 999L;

            when(bookmarkRepository.findByIdAndUserId(invalidBookmarkId, userId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookmarkService.deleteBookmark(userId, invalidBookmarkId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Bookmark");
        }

        @Test
        @DisplayName("북마크 이동 실패: 다른 사람의 폴더로 이동 시도")
        void moveBookmark_fail_accessDenied() {
            // given
            Long userId = 1L;
            Long otherUserId = 2L;
            Long targetFolderId = 50L;

            User user = User.builder().id(userId).build();
            User otherUser = User.builder().id(otherUserId).build();

            Bookmark bookmark = Bookmark.builder().id(10L).user(user).build();
            BookmarkFolder otherFolder = BookmarkFolder.builder().id(targetFolderId).user(otherUser).build();

            when(bookmarkRepository.findByIdAndUserId(10L, userId)).thenReturn(Optional.of(bookmark));
            when(folderRepository.findById(targetFolderId)).thenReturn(Optional.of(otherFolder));

            // when & then
            assertThatThrownBy(() -> bookmarkService.moveBookmark(userId, 10L, targetFolderId))
                    .isInstanceOf(FolderAccessDeniedException.class);
        }

        @Test
        @DisplayName("폴더에서 제거 성공")
        void removeBookmarkFromFolder_success() {
            // given
            Long userId = 1L;
            Long bookmarkId = 10L;
            BookmarkFolder folder = BookmarkFolder.builder().id(5L).build();
            Bookmark bookmark = Bookmark.builder().id(bookmarkId).bookmarkFolder(folder).build();

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, userId)).thenReturn(Optional.of(bookmark));

            // when
            bookmarkService.removeBookmarkFromFolder(userId, bookmarkId);

            // then
            assertThat(bookmark.getBookmarkFolder()).isNull();
        }

        @Test
        @DisplayName("북마크 이동 성공: 폴더 지정 없이 이동 (folderId가 null일 때 -> 미분류로 이동)")
        void moveBookmark_success_toNullFolder() {
            // given
            Long userId = 1L;
            Long bookmarkId = 10L;
            Long targetFolderId = null; // null 전달

            User user = User.builder().id(userId).build();

            // 기존에는 어떤 폴더에 들어있던 북마크라고 가정
            BookmarkFolder oldFolder = BookmarkFolder.builder().id(99L).build();
            Bookmark bookmark = Bookmark.builder()
                    .id(bookmarkId)
                    .user(user)
                    .bookmarkFolder(oldFolder)
                    .build();

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, userId)).thenReturn(Optional.of(bookmark));

            // when
            bookmarkService.moveBookmark(userId, bookmarkId, targetFolderId);

            // then
            // 1. 북마크의 폴더가 null로 변경되었는지 확인
            assertThat(bookmark.getBookmarkFolder()).isNull();

            // 2. folderId가 null이므로 폴더 조회 로직(DB 접근)이 실행되지 않아야 함
            verify(folderRepository, never()).findById(anyLong());
        }
    }

    @Test
    @DisplayName("폴더 목록 조회 (getFolders)")
    void getFolders_success() {
        // given
        Long userId = 1L;
        BookmarkFolder f1 = BookmarkFolder.builder().id(1L).name("F1").build();
        BookmarkFolder f2 = BookmarkFolder.builder().id(2L).name("F2").build();

        when(folderRepository.findAllByUserIdOrderById(userId)).thenReturn(List.of(f1, f2));

        // when
        List<BookmarkFolderResponseDto> result = bookmarkService.getFolders(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("F1");
        assertThat(result.get(1).getName()).isEqualTo("F2");
    }

    @Nested
    @DisplayName("북마크 Map 조회")
    class GetBookmarkMap {

        @Test
        @DisplayName("성공: 요청한 컨텐츠 ID에 해당하는 북마크 Map(ContentId -> BookmarkId) 반환")
        void getBookmarkMap_success() {
            // given
            Long userId = 1L;
            List<String> contentIds = List.of("100", "200");

            Bookmark b1 = Bookmark.builder().id(10L).contentId("100").build();
            Bookmark b2 = Bookmark.builder().id(20L).contentId("200").build();

            when(bookmarkRepository.findAllByUserIdAndContentIdIn(userId, contentIds))
                    .thenReturn(List.of(b1, b2));

            // when
            Map<String, Long> result = bookmarkService.getBookmarkMap(userId, contentIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get("100")).isEqualTo(10L);
            assertThat(result.get("200")).isEqualTo(20L);
        }

        @Test
        @DisplayName("성공: 조회된 북마크가 없으면 빈 Map 반환")
        void getBookmarkMap_success_noResult() {
            // given
            Long userId = 1L;
            List<String> contentIds = List.of("999");

            when(bookmarkRepository.findAllByUserIdAndContentIdIn(userId, contentIds))
                    .thenReturn(Collections.emptyList());

            // when
            Map<String, Long> result = bookmarkService.getBookmarkMap(userId, contentIds);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공: 입력 리스트가 비어있으면 DB 조회 없이 빈 Map 반환")
        void getBookmarkMap_emptyInput() {
            // given
            Long userId = 1L;
            List<String> contentIds = Collections.emptyList();

            // when
            Map<String, Long> result = bookmarkService.getBookmarkMap(userId, contentIds);

            // then
            assertThat(result).isEmpty();
            verify(bookmarkRepository, never()).findAllByUserIdAndContentIdIn(anyLong(), anyList());
        }

        @Test
        @DisplayName("성공: 입력 리스트가 null이면 DB 조회 없이 빈 Map 반환")
        void getBookmarkMap_nullInput() {
            // given
            Long userId = 1L;

            // when
            Map<String, Long> result = bookmarkService.getBookmarkMap(userId, null);

            // then
            assertThat(result).isEmpty();
            verify(bookmarkRepository, never()).findAllByUserIdAndContentIdIn(anyLong(), anyList());
        }
    }

}