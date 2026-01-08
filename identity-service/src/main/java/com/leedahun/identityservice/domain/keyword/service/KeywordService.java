package com.leedahun.identityservice.domain.keyword.service;

import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import java.util.List;
import java.util.Set;

public interface KeywordService {

    List<KeywordResponseDto> getKeywords(Long userId);

    KeywordResponseDto addKeyword(Long userId, String name);

    KeywordResponseDto toggleKeywordNotification(Long userId, Long keywordId);

    void deleteKeyword(Long userId, Long keywordId);

    List<Long> findUserIdsByKeywords(Set<String> keywords);

}
