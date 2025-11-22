package com.leedahun.identityservice.domain.keyword.controller;

import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.service.KeywordService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users/")
@RequiredArgsConstructor
public class KeywordInternalController {

    private final KeywordService keywordService;

    @GetMapping("/{userId}/keywords")
    public List<KeywordResponseDto> getActiveKeywords(@PathVariable("userId") Long userId) {
        return keywordService.getKeywords(userId);
    }

}
