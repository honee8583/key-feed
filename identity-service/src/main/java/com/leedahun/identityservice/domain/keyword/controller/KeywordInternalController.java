package com.leedahun.identityservice.domain.keyword.controller;

import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.service.KeywordService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class KeywordInternalController {

    private final KeywordService keywordService;

    @GetMapping("/users/{userId}/keywords")
    public List<KeywordResponseDto> getActiveKeywords(@PathVariable("userId") Long userId) {
        return keywordService.getKeywords(userId);
    }

    @PostMapping("/keywords/match-users")
    public List<Long> findUserIdsByKeywords(@RequestBody Set<String> keywords) {
        return keywordService.findUserIdsByKeywords(keywords);
    }

}
