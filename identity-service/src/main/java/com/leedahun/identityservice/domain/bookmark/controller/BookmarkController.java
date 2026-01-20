package com.leedahun.identityservice.domain.bookmark.controller;

import com.leedahun.identityservice.common.message.SuccessMessage;
import com.leedahun.identityservice.common.response.CursorPage;
import com.leedahun.identityservice.common.response.HttpResponse;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkFolderRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkFolderResponseDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkMoveRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkResponseDto;
import com.leedahun.identityservice.domain.bookmark.service.BookmarkService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 북마크 폴더 생성
     */
    @PostMapping("/folders")
    public ResponseEntity<?> createFolder(@AuthenticationPrincipal Long userId,
                                          @Valid @RequestBody BookmarkFolderRequestDto request) {
        Long folderId = bookmarkService.createFolder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpResponse(HttpStatus.CREATED, SuccessMessage.WRITE_SUCCESS.getMessage(), folderId));
    }

    /**
     * 북마크 폴더 수정
     */
    @PatchMapping("/folders/{folderId}")
    public ResponseEntity<?> updateFolder(@AuthenticationPrincipal Long userId,
                                          @PathVariable("folderId") Long folderId,
                                          @Valid @RequestBody BookmarkFolderRequestDto request) {
        bookmarkService.updateFolder(userId, folderId, request);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.UPDATE_SUCCESS.getMessage(), null));
    }

    /**
     * 북마크 폴더 삭제
     */
    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<?> deleteFolder(@AuthenticationPrincipal Long userId,
                                          @PathVariable("folderId") Long folderId) {
        bookmarkService.deleteFolder(userId, folderId);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.DELETE_SUCCESS.getMessage(), null));
    }

    /**
     * 북마크를 폴더에서 제거
     */
    @DeleteMapping("/{bookmarkId}/folder")
    public ResponseEntity<?> removeBookmarkFromFolder(@AuthenticationPrincipal Long userId, @PathVariable Long bookmarkId) {
        bookmarkService.removeBookmarkFromFolder(userId, bookmarkId);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.UPDATE_SUCCESS.getMessage(), null));
    }

    /**
     * 북마크 폴더 이동
     */
    @PatchMapping("/{bookmarkId}/folder")
    public ResponseEntity<?> moveBookmark(@AuthenticationPrincipal Long userId,
                                          @PathVariable Long bookmarkId,
                                          @RequestBody BookmarkMoveRequestDto request) {
        bookmarkService.moveBookmark(userId, bookmarkId, request.getFolderId());
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.UPDATE_SUCCESS.getMessage(), null));
    }

    /**
     * 북마크 폴더 목록 조회
     */
    @GetMapping("/folders")
    public ResponseEntity<?> getFolders(@AuthenticationPrincipal Long userId) {
        List<BookmarkFolderResponseDto> folders = bookmarkService.getFolders(userId);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.READ_SUCCESS.getMessage(), folders));
    }

    /**
     * 북마크 등록
     */
    @PostMapping
    public ResponseEntity<?> addBookmark(@AuthenticationPrincipal Long userId,
                                         @Valid @RequestBody BookmarkRequestDto request) {
        Long bookmarkId = bookmarkService.addBookmark(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpResponse(HttpStatus.CREATED, SuccessMessage.WRITE_SUCCESS.getMessage(), bookmarkId));
    }

    /**
     * 북마크 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getBookmarks(@AuthenticationPrincipal Long userId,
                                          @RequestParam(required = false) Long folderId,
                                          @RequestParam(required = false) Long lastId,
                                          @RequestParam(defaultValue = "20") int size) {
        CursorPage<BookmarkResponseDto> response = bookmarkService.getBookmarks(userId, lastId, folderId, size);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.READ_SUCCESS.getMessage(), response));
    }

    /**
     * 북마크 해제
     */
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<?> deleteBookmark(@AuthenticationPrincipal Long userId, @PathVariable Long bookmarkId) {
        bookmarkService.deleteBookmark(userId, bookmarkId);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.DELETE_SUCCESS.getMessage(), null));
    }

}
