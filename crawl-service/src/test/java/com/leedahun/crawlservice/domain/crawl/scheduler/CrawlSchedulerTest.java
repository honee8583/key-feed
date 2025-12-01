package com.leedahun.crawlservice.domain.crawl.scheduler;

import com.leedahun.crawlservice.domain.crawl.entity.Source;
import com.leedahun.crawlservice.domain.crawl.repository.SourceRepository;
import com.leedahun.crawlservice.domain.crawl.service.CrawlService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrawlSchedulerTest {

    @InjectMocks
    private CrawlScheduler crawlScheduler;

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private CrawlService crawlService;

    @Test
    @DisplayName("스케줄러가 실행되면 수집 대상 소스를 조회하고 각 소스에 대해 크롤링을 수행한다")
    void scheduleCrawling_Success() {
        // given
        Source source1 = Source.builder().id(1L).url("https://blog1.com/feed").build();
        Source source2 = Source.builder().id(2L).url("https://blog2.com/feed").build();
        List<Source> sources = List.of(source1, source2);

        // 10분 전 시간보다 오래된 소스들을 찾는다고 가정
        given(sourceRepository.findSourcesToCrawl(any(LocalDateTime.class)))
                .willReturn(sources);

        // when
        crawlScheduler.scheduleCrawling();

        // then
        // 1. 리포지토리에서 조회 메서드가 호출되었는지 검증
        verify(sourceRepository, times(1)).findSourcesToCrawl(any(LocalDateTime.class));

        // 2. 조회된 소스(2개) 각각에 대해 processSource가 호출되었는지 검증
        verify(crawlService, times(1)).processSource(source1);
        verify(crawlService, times(1)).processSource(source2);
    }

    @Test
    @DisplayName("수집 대상 소스가 없으면 크롤링 로직을 수행하지 않고 종료한다")
    void scheduleCrawling_NoSources() {
        // given
        // 크롤링 대상 없음
        given(sourceRepository.findSourcesToCrawl(any(LocalDateTime.class))).willReturn(Collections.emptyList());

        // when
        crawlScheduler.scheduleCrawling();

        // then
        verify(sourceRepository, times(1)).findSourcesToCrawl(any(LocalDateTime.class));

        // 소스가 없으므로 processSource는 단 한 번도 호출되지 않아야 함
        verify(crawlService, never()).processSource(any());
    }

    @Test
    @DisplayName("특정 소스 크롤링 중 예외가 발생해도 다른 소스의 크롤링은 계속 진행된다")
    void scheduleCrawling_ContinueOnError() {
        // given
        Source source1 = Source.builder()
                .id(1L)
                .url("https://error-blog.com")
                .build(); // 에러 발생용
        Source source2 = Source.builder()
                .id(2L)
                .url("https://normal-blog.com")
                .build(); // 정상
        List<Source> sources = List.of(source1, source2);

        given(sourceRepository.findSourcesToCrawl(any(LocalDateTime.class))).willReturn(sources);

        // 예외 발생
        doThrow(new RuntimeException("Connection Timeout")).when(crawlService).processSource(source1);

        // when
        crawlScheduler.scheduleCrawling();

        // then
        // 예외가 발생했던 source1에 대해서도 호출은 시도되었음
        verify(crawlService).processSource(source1);

        // source1에서 에러가 났지만, source2에 대한 호출도 정상적으로 이루어져야 함
        verify(crawlService).processSource(source2);
    }

    @Test
    @DisplayName("크롤링 작업이 10분 내에 완료되지 않으면 강제 종료한다")
    void scheduleCrawling_Timeout() throws InterruptedException {
        // given
        Source source = Source.builder().id(1L).url("https://slow-blog.com").build();
        given(sourceRepository.findSourcesToCrawl(any(LocalDateTime.class)))
                .willReturn(List.of(source));

        ExecutorService mockExecutor = mock(ExecutorService.class);

        // Executors.newFixedThreadPool 호출 시 Mock Executor 반환
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newFixedThreadPool(anyInt())).thenReturn(mockExecutor);

            // awaitTermination 호출 시 false 반환 설정
            when(mockExecutor.awaitTermination(10, TimeUnit.MINUTES)).thenReturn(false);

            // when
            crawlScheduler.scheduleCrawling();

            // then
            // 작업 제출 확인
            verify(mockExecutor).submit(any(Runnable.class));

            // 정상 종료 시도 확인
            verify(mockExecutor).shutdown();

            // 타임아웃 발생으로 인한 강제 종료 확인
            verify(mockExecutor).shutdownNow();
        }
    }

    @Test
    @DisplayName("크롤링 작업 대기 중 인터럽트가 발생하면 강제 종료하고 인터럽트 상태를 복구한다")
    void scheduleCrawling_Interrupted() throws InterruptedException {
        // given
        Source source = Source.builder()
                .id(1L)
                .url("https://blog.com")
                .build();
        given(sourceRepository.findSourcesToCrawl(any(LocalDateTime.class))).willReturn(List.of(source));

        ExecutorService mockExecutor = mock(ExecutorService.class);
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newFixedThreadPool(anyInt())).thenReturn(mockExecutor);

            // awaitTermination 호출 시 InterruptedException 발생 설정
            when(mockExecutor.awaitTermination(10, TimeUnit.MINUTES)).thenThrow(new InterruptedException("Interrupted!"));

            // when
            crawlScheduler.scheduleCrawling();

            // then
            // 예외 발생 시에도 강제 종료가 호출되어야 함
            verify(mockExecutor).shutdownNow();

            // 현재 스레드의 인터럽트 상태가 복구되었는지 확인
            assertThat(Thread.currentThread().isInterrupted()).isTrue();

            Thread.interrupted();
        }
    }
}