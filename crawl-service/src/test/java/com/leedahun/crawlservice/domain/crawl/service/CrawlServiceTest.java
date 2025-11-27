package com.leedahun.crawlservice.domain.crawl.service;

import com.leedahun.crawlservice.domain.crawl.dto.CrawledContentDto;
import com.leedahun.crawlservice.domain.crawl.dto.FeedItem;
import com.leedahun.crawlservice.domain.crawl.entity.Source;
import com.leedahun.crawlservice.domain.crawl.repository.SourceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrawlServiceTest {

    @InjectMocks
    private CrawlService crawlService;

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private RssFeedParser rssFeedParser;

    @Mock
    private KafkaTemplate<String, CrawledContentDto> kafkaTemplate;

    private static final String TEST_URL = "https://test-blog.com/feed";
    private static final String TOPIC_NAME = "content-topic";

    @Test
    @DisplayName("새로운 글이 발견되면 Kafka로 발행하고 Source의 상태를 최신 글 Hash로 업데이트한다")
    void processSource_NewItemsFound() {
        // given
        String oldHash = "hash-1";
        String newHash = "hash-2";

        // DB에 저장된 소스 (마지막으로 hash-1까지 수집함)
        Source source = Source.builder()
                .id(1L)
                .url(TEST_URL)
                .lastItemHash(oldHash)
                .lastCrawledAt(LocalDateTime.now().minusMinutes(10))
                .build();

        // RSS 파서가 가져온 피드 목록 (최신순: hash-2 -> hash-1)
        FeedItem newItem = createFeedItem(newHash, "New Title");
        FeedItem oldItem = createFeedItem(oldHash, "Old Title");
        List<FeedItem> feedItems = List.of(newItem, oldItem);

        given(rssFeedParser.parse(TEST_URL)).willReturn(feedItems);

        // when
        crawlService.processSource(source);

        // then
        // Kafka로 '새 글(hash-2)' 1건이 전송되었는지 검증
        verify(kafkaTemplate, times(1)).send(eq(TOPIC_NAME), any(CrawledContentDto.class));

        // Source 상태 업데이트 검증
        // lastItemHash가 'newHash'로 변경되었는지 확인
        assertThat(source.getLastItemHash()).isEqualTo(newHash);

        // lastCrawledAt이 갱신되었는지 확인
        assertThat(source.getLastCrawledAt()).isNotNull();

        // DB 저장 호출 검증
        verify(sourceRepository, times(1)).save(source);
    }

    @Test
    @DisplayName("새로운 글이 없으면 Kafka로 발행하지 않고 Source의 수집 시간만 업데이트한다")
    void processSource_NoNewItems() {
        // given
        String currentHash = "hash-latest";

        // 이미 최신 글(hash-latest)까지 수집됨
        Source source = Source.builder()
                .id(1L)
                .url(TEST_URL)
                .lastItemHash(currentHash)
                .build();

        // RSS 파서: 최신 글이 DB와 동일함
        FeedItem latestItem = createFeedItem(currentHash, "Latest Title");
        List<FeedItem> feedItems = List.of(latestItem);

        given(rssFeedParser.parse(TEST_URL)).willReturn(feedItems);

        // when
        crawlService.processSource(source);

        // then
        // Kafka 전송은 일어나지 않아야 함
        verify(kafkaTemplate, never()).send(any(), any());

        // Source 상태 검증
        // Hash는 그대로, 시간은 갱신
        assertThat(source.getLastItemHash()).isEqualTo(currentHash);


        // DB 저장 호출 검증
        verify(sourceRepository, times(1)).save(source);
    }

    @Test
    @DisplayName("RSS 피드가 비어있으면 상태 업데이트(시간)만 수행한다")
    void processSource_EmptyFeed() {
        // given
        Source source = Source.builder()
                .id(1L)
                .url(TEST_URL)
                .lastItemHash("some-hash")
                .build();

        // 새 글을 수집하지 않음
        given(rssFeedParser.parse(TEST_URL)).willReturn(Collections.emptyList());

        // when
        crawlService.processSource(source);

        // then
        verify(kafkaTemplate, never()).send(any(), any()); // 카프카 전송 x
        verify(sourceRepository, times(1)).save(source);
    }

    @Test
    @DisplayName("최초 수집(Hash가 null)일 경우 모든 글을 수집하고 최신 Hash를 저장한다")
    void processSource_FirstCrawl() {
        // given
        Source source = Source.builder()
                .id(1L)
                .url(TEST_URL)
                .lastItemHash(null) // 최초 수집
                .build();

        FeedItem item1 = createFeedItem("hash-2", "Title 2");
        FeedItem item2 = createFeedItem("hash-1", "Title 1");
        List<FeedItem> feedItems = List.of(item1, item2);

        given(rssFeedParser.parse(TEST_URL)).willReturn(feedItems);

        // when
        crawlService.processSource(source);

        // then
        // 2개의 글 모두 Kafka 전송
        verify(kafkaTemplate, times(2)).send(eq(TOPIC_NAME), any(CrawledContentDto.class));

        // Hash는 가장 최신 글(hash-2)로 업데이트
        assertThat(source.getLastItemHash()).isEqualTo("hash-2");

        // DB 저장
        verify(sourceRepository, times(1)).save(source);
    }

    private FeedItem createFeedItem(String guid, String title) {
        return FeedItem.builder()
                .guid(guid)
                .title(title)
                .link("https://link.com/" + guid)
                .summary("Summary...")
                .thumbnailUrl("https://thumb.com/img.jpg")
                .pubDate(LocalDateTime.now())
                .build();
    }
}