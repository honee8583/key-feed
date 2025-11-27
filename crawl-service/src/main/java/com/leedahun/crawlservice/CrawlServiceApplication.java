package com.leedahun.crawlservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CrawlServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlServiceApplication.class, args);
    }

}
