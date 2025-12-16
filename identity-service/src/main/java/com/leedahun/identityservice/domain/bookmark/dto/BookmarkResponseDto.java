package com.leedahun.identityservice.domain.bookmark.dto;

import com.leedahun.identityservice.domain.bookmark.entity.Bookmark;
import com.leedahun.identityservice.domain.bookmark.entity.BookmarkFolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkResponseDto {
    private Long bookmarkId;
    private Long contentId;
    private Long folderId;
    private String folderName;
    private LocalDateTime createdAt;

    public static BookmarkResponseDto from(Bookmark bookmark) {
        BookmarkFolder folder = bookmark.getBookmarkFolder();

        Long folderId = null;
        String folderName = null;
        if (folder != null) {
            folderId = folder.getId();
            folderName = folder.getName();
        }

        return BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .contentId(bookmark.getContentId())
                .folderId(folderId)
                .folderName(folderName)
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
