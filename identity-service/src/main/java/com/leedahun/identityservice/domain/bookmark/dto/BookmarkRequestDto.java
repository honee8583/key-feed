package com.leedahun.identityservice.domain.bookmark.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookmarkRequestDto {
    private Long contentId;
    private Long folderId;
}
