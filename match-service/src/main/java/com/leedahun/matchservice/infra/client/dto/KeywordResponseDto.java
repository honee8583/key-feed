package com.leedahun.matchservice.infra.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KeywordResponseDto {
    private Long keywordId;
    private String name;
    private Boolean isNotificationEnabled;
}
