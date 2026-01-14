package com.leedahun.identityservice.domain.bookmark.controller;

import com.leedahun.identityservice.domain.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/bookmarks")
@RequiredArgsConstructor
public class BookmarkInternalController {

    private final BookmarkService bookmarkService;

    @PostMapping("/user/{userId}/check")
    public Map<String, Long> getBookmarkMap(@PathVariable Long userId, @RequestBody List<String> contentIds) {
        return bookmarkService.getBookmarkMap(userId, contentIds);
    }

}