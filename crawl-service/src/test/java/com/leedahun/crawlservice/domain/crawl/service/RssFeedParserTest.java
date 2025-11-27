package com.leedahun.crawlservice.domain.crawl.service;

import com.leedahun.crawlservice.domain.crawl.dto.FeedItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RssFeedParserTest {

    private final RssFeedParser rssFeedParser = new RssFeedParser();

    // JUnit 5에서 임시 디렉토리를 생성해주고 테스트 후 삭제해주는 어노테이션
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("정상적인 RSS 피드를 파싱하여 FeedItem 리스트를 반환한다")
    void parse_Success() throws IOException {
        // given
        // HTML 태그와 이미지가 포함된 RSS 샘플
        String rssContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                <channel>
                 <title>Test Tech Blog</title>
                 <item>
                  <title>Spring Boot 3.0 Update</title>
                  <link>https://test.com/spring-boot-3</link>
                  <pubDate>Tue, 19 Nov 2024 10:00:00 +0000</pubDate>
                  <description><![CDATA[
                    <div class="content">
                        <p>Spring Boot 3.0 has been <b>released</b>.</p>
                        <img src="https://test.com/thumbnail.jpg" alt="thumbnail" />
                        <p>Check it out!</p>
                    </div>
                  ]]></description>
                  <guid>post-1</guid>
                 </item>
                </channel>
                </rss>
                """;

        String feedUrl = createTempRssFile(rssContent);

        // when
        List<FeedItem> result = rssFeedParser.parse(feedUrl);

        // then
        assertThat(result).hasSize(1);
        FeedItem item = result.get(0);

        // 추출 값 확인
        assertThat(item.getTitle()).isEqualTo("Spring Boot 3.0 Update");
        assertThat(item.getLink()).isEqualTo("https://test.com/spring-boot-3");
        assertThat(item.getGuid()).isEqualTo("post-1");
        assertThat(item.getSummary()).contains("Spring Boot 3.0 has been released.");
        assertThat(item.getSummary()).doesNotContain("<div>", "<b>", "<img");

        // 이미지 추출 검증
        assertThat(item.getThumbnailUrl()).isEqualTo("https://test.com/thumbnail.jpg");
        assertThat(item.getPubDate()).isNotNull();
    }

    @Test
    @DisplayName("XML 제어 문자(0x1c)가 포함된 피드도 에러 없이 파싱해야 한다 (Sanitization 검증)")
    void parse_InvalidXmlCharacter() throws IOException {
        // given
        // 파싱 에러를 유발하는 File Separator (0x1c) 문자 삽입
        char invalidChar = 0x1c;
        String dirtyRssContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                <channel>
                 <item>
                  <title>Dirty Title""" + invalidChar + """
                  </title>
                  <link>https://test.com/dirty</link>
                  <description>Clean Content</description>
                 </item>
                </channel>
                </rss>
                """;

        String feedUrl = createTempRssFile(dirtyRssContent);

        // when
        List<FeedItem> result = rssFeedParser.parse(feedUrl);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Dirty Title"); // 제어 문자가 제거된 상태로 파싱됨
    }

    @Test
    @DisplayName("본문에 이미지가 없고 Enclosure 태그에 이미지가 있는 경우 이를 썸네일로 사용한다")
    void parse_EnclosureImage() throws IOException {
        // given
        String rssContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                <channel>
                 <item>
                  <title>Enclosure Test</title>
                  <link>https://test.com/enc</link>
                  <description>No image here.</description>
                  <enclosure url="https://test.com/enclosure-thumb.png" type="image/png" length="1234" />
                 </item>
                </channel>
                </rss>
                """;

        String feedUrl = createTempRssFile(rssContent);

        // when
        List<FeedItem> result = rssFeedParser.parse(feedUrl);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getThumbnailUrl()).isEqualTo("https://test.com/enclosure-thumb.png");
        assertThat(result.get(0).getSummary()).isEqualTo("No image here.");
    }

    @Test
    @DisplayName("잘못된 URL이나 파일이 없는 경우 빈 리스트를 반환하고 예외를 발생시키지 않는다")
    void parse_InvalidUrl() {
        // given
        String invalidUrl = "http://invalid-url-that-does-not-exist.com/rss";

        // when
        List<FeedItem> result = rssFeedParser.parse(invalidUrl);

        // then
        // 에러 로그가 찍히고 메서드 자체는 빈 리스트를 반환
        assertThat(result).isEmpty();
    }

    // 실제 url에 접속하는 것이 아닌 테스트룰 위해 가짜 웹 서버 역할을 대신하는 파일 생성기
    private String createTempRssFile(String content) throws IOException {
        Path file = tempDir.resolve("feed.xml");
        Files.writeString(file, content);
        return file.toUri().toURL().toString();  // 로컬 파일 경로를 URL 형태(file:/...)로 변환
    }
}