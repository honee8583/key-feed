package com.leedahun.identityservice.domain.keyword.service;

import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import java.util.List;

public interface KeywordService {

    List<KeywordResponseDto> getKeywords(Long userId);

    KeywordResponseDto addKeyword(Long userId, String name);

    KeywordResponseDto toggleKeywordNotification(Long userId, Long keywordId);

    void deleteKeyword(Long userId, Long keywordId);

}
