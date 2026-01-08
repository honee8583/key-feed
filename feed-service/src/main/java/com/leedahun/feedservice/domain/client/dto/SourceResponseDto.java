package com.leedahun.feedservice.domain.client.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SourceResponseDto {
    private Long sourceId;
    private Long userSourceId;
    private String userDefinedName;
    private String url;
}
