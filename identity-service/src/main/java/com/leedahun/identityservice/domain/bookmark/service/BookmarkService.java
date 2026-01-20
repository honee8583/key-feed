package com.leedahun.identityservice.domain.bookmark.service;

import com.leedahun.identityservice.common.response.CursorPage;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkFolderRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkFolderResponseDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkResponseDto;
import java.util.List;
import java.util.Map;

public interface BookmarkService {

    Long createFolder(Long userId, BookmarkFolderRequestDto request);

    void updateFolder(Long userId, Long folderId, BookmarkFolderRequestDto request);

    void deleteFolder(Long userId, Long folderId);

    Long addBookmark(Long userId, BookmarkRequestDto request);

    CursorPage<BookmarkResponseDto> getBookmarks(Long userId, Long lastId, Long folderId, int size);

    void deleteBookmark(Long userId, Long bookmarkId);

    void removeBookmarkFromFolder(Long userId, Long bookmarkId);

    void moveBookmark(Long userId, Long bookmarkId, Long folderId);

    List<BookmarkFolderResponseDto> getFolders(Long userId);

    Map<String, Long> getBookmarkMap(Long userId, List<String> contentIds);

}
