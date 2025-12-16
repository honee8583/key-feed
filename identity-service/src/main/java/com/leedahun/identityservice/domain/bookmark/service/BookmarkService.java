package com.leedahun.identityservice.domain.bookmark.service;

import com.leedahun.identityservice.common.response.CursorPage;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkFolderRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkResponseDto;

public interface BookmarkService {

    Long createFolder(Long userId, BookmarkFolderRequestDto request);

    Long addBookmark(Long userId, BookmarkRequestDto request);

    CursorPage<BookmarkResponseDto> getBookmarks(Long userId, Long lastId, Long folderId, int size);

    void deleteBookmark(Long userId, Long bookmarkId);

    void removeBookmarkFromFolder(Long userId, Long bookmarkId);

    void moveBookmark(Long userId, Long bookmarkId, Long folderId);

}
