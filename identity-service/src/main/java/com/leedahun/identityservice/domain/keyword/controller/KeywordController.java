package com.leedahun.identityservice.domain.keyword.controller;

import com.leedahun.identityservice.common.message.SuccessMessage;
import com.leedahun.identityservice.common.response.HttpResponse;
import com.leedahun.identityservice.domain.keyword.dto.KeywordCreateRequestDto;
import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping
    public ResponseEntity<?> getMyKeywords(@AuthenticationPrincipal Long userId) {
        List<KeywordResponseDto> keywords = keywordService.getKeywords(userId);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.READ_SUCCESS.getMessage(), keywords));
    }

    @PostMapping
    public ResponseEntity<?> addKeyword(@AuthenticationPrincipal Long userId,
                                        @RequestBody KeywordCreateRequestDto keywordCreateRequest) {
        KeywordResponseDto addKeywordResult = keywordService.addKeyword(userId, keywordCreateRequest.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpResponse(HttpStatus.CREATED, SuccessMessage.WRITE_SUCCESS.getMessage(), addKeywordResult));
    }

    @PatchMapping("/{keywordId}/toggle")
    public ResponseEntity<?> toggleKeywordNotification(@AuthenticationPrincipal Long userId,
                                                       @PathVariable("keywordId") Long keywordId) {
        KeywordResponseDto updateKeywordResult = keywordService.toggleKeywordNotification(userId, keywordId);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.UPDATE_SUCCESS.getMessage(), updateKeywordResult));
    }

    @DeleteMapping("/{keywordId}")
    public ResponseEntity<?> deleteKeyword(@AuthenticationPrincipal Long userId,
                                           @PathVariable("keywordId") Long keywordId) {
        keywordService.deleteKeyword(userId, keywordId);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.DELETE_SUCCESS.getMessage(), null));
    }

}