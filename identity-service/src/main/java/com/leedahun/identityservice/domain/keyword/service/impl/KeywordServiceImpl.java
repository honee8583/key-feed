package com.leedahun.identityservice.domain.keyword.service.impl;

import com.leedahun.identityservice.common.error.exception.EntityAlreadyExistsException;
import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.entity.Keyword;
import com.leedahun.identityservice.domain.keyword.exception.KeywordLimitExceededException;
import com.leedahun.identityservice.domain.keyword.repository.KeywordRepository;
import com.leedahun.identityservice.domain.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;

    @Value("${app.limits.keyword-max-count}")
    private int keywordMaxCount;

    @Override
    @Transactional(readOnly = true)
    public List<KeywordResponseDto> getKeywords(Long userId) {
        List<Keyword> keywords = keywordRepository.findByUserId(userId);
        return keywords.stream()
                .map(KeywordResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public KeywordResponseDto addKeyword(Long userId, String name) {
        User user = findUserById(userId);

        if (keywordRepository.existsByNameAndUser(name, user)) {
            throw new EntityAlreadyExistsException("Keyword", name);
        }

        if (keywordRepository.countByUserId(userId) >= keywordMaxCount) {
            throw new KeywordLimitExceededException();
        }

        Keyword keyword = Keyword.builder()
                .user(user)
                .name(name)
                .isNotificationEnabled(true)
                .build();
        keywordRepository.save(keyword);

        return KeywordResponseDto.from(keyword);
    }

    @Override
    @Transactional
    public KeywordResponseDto toggleKeywordNotification(Long userId, Long keywordId) {
        Keyword keyword = findKeywordByIdAndUserId(keywordId, userId);
        keyword.setNotificationEnabled(!keyword.isNotificationEnabled());
        keywordRepository.save(keyword);

        return KeywordResponseDto.from(keyword);
    }

    @Override
    @Transactional
    public void deleteKeyword(Long userId, Long keywordId) {
        Keyword keyword = findKeywordByIdAndUserId(keywordId, userId);
        keywordRepository.delete(keyword);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
    }

    private Keyword findKeywordByIdAndUserId(Long keywordId, Long userId) {
        return keywordRepository.findByIdAndUserId(keywordId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Keyword", keywordId));
    }

}