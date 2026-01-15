package com.leedahun.matchservice;

import com.leedahun.matchservice.domain.content.repository.ContentDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MatchServiceApplicationTests {

    @MockitoBean
    private ContentDocumentRepository contentDocumentRepository;

    @Test
    void contextLoads() {
    }

}
