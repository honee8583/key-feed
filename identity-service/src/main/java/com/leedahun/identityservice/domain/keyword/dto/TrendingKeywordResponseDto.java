package com.leedahun.identityservice.domain.keyword.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TrendingKeywordResponseDto {
    private String getName;
    private Long getCount;
}
