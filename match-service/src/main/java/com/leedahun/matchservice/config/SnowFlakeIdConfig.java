package com.leedahun.matchservice.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowFlakeIdConfig {

    @Bean
    public Snowflake snowflake(
            @Value("${snowflake.worker-id}") long workerId,
            @Value("${snowflake.datacenter-id}") long datacenterId
    ) {
        return IdUtil.getSnowflake(workerId, datacenterId);
    }

}
