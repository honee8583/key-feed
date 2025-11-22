package com.leedahun.identityservice.domain.auth.controller;

import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.entity.Keyword;
import com.leedahun.identityservice.domain.keyword.repository.KeywordRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users/")
@RequiredArgsConstructor
public class UserController {

    private final KeywordRepository keywordRepository;

    @GetMapping("/{userId}/keywords")
    public List<KeywordResponseDto> getActiveKeywords(@PathVariable("userId") Long userId) {
        List<Keyword> keywords = keywordRepository.findByUserId(userId);
        return keywords.stream()
                .map(KeywordResponseDto::from)
                .collect(Collectors.toList());
    }

}
