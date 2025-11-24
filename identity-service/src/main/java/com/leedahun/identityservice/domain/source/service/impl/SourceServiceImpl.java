package com.leedahun.identityservice.domain.source.service.impl;

import com.leedahun.identityservice.common.error.exception.EntityAlreadyExistsException;
import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.source.dto.SourceRequestDto;
import com.leedahun.identityservice.domain.source.dto.SourceResponseDto;
import com.leedahun.identityservice.domain.source.entity.Source;
import com.leedahun.identityservice.domain.source.entity.UserSource;
import com.leedahun.identityservice.domain.source.exception.InvalidRssUrlException;
import com.leedahun.identityservice.domain.source.repository.SourceRepository;
import com.leedahun.identityservice.domain.source.repository.UserSourceRepository;
import com.leedahun.identityservice.domain.source.service.SourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;
    private final UserSourceRepository userSourceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SourceResponseDto> getSourcesByUser(Long userId) {
        List<UserSource> userSources = userSourceRepository.findByUserId(userId);
        return userSources.stream()
                .map(SourceResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public SourceResponseDto addSource(Long userId, SourceRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        // RSS 피드 주소 탐지
        String rssUrl = discoverRssUrl(request.getUrl());
        log.info("Discovered RSS URL: {} -> {}", request.getUrl(), rssUrl);

        // 기존에 같은 source가 존재하지 않는다면 생성
        Source source = sourceRepository.findByUrl(rssUrl)
                .orElseGet(() -> {
                    Source newSource = Source.builder()
                            .url(rssUrl)
                            .build();
                    return sourceRepository.save(newSource);
                });

        // 이미 내가 등록한 소스인지 확인
        if (userSourceRepository.existsByUserIdAndSourceId(userId, source.getId())) {
            throw new EntityAlreadyExistsException("UserSource", "userId: " + userId + ", sourceId: " + source.getId());
        }

        UserSource userSource = UserSource.builder()
                .user(user)
                .source(source)
                .userDefinedName(request.getName())
                .build();
        userSourceRepository.save(userSource);

        return SourceResponseDto.from(userSource);
    }

    @Override
    public void removeUserSource(Long userId, Long userSourceId) {
        UserSource userSource = userSourceRepository.findByIdAndUserId(userSourceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("UserSource", userSourceId));
        userSourceRepository.delete(userSource);
    }

    private String discoverRssUrl(String inputUrl) {
        try {
            // HTML 문저 전체
            Document doc = Jsoup.connect(inputUrl)
                    .timeout(5000)
                    .get();

            // RSS 2.0 피드를 나타내는 표준 MIME 타입
            // 대부분의 블로그, 뉴스사이트는 RSS주소를 알리기 위해 head태그안에 다음의 태그를 넣어둔다.
            // <link rel="alternate" type="application/rss+xml" title="RSS Feed" href="https://techblog.woowahan.com/feed/" />
            Element rssLink = doc.select("link[type=application/rss+xml]").first();
            if (rssLink != null) {
                return rssLink.attr("abs:href");
            }

            Element atomLink = doc.select("link[type=application/atom+xml]").first();
            if (atomLink != null) {
                return atomLink.attr("abs:href");
            }

            return inputUrl;

        } catch (IOException e) {
            log.warn("URL 접속 실패: {}. 원인: {}", inputUrl, e.getMessage());
            throw new InvalidRssUrlException();
        } catch (Exception e) {
            log.warn("URL 자동 탐색 파싱 실패: {}. 원본 입력을 대신 사용합니다. 오류: {}", inputUrl, e.toString());
            return inputUrl;
        }
    }
}