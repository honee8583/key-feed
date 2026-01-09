package com.leedahun.feedservice;

import com.leedahun.feedservice.domain.feed.repository.ContentDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class FeedServiceApplicationTests {

    @MockitoBean
    private ContentDocumentRepository contentDocumentRepository;

    @Test
    void contextLoads() {
    }

}
