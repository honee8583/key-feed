package com.leedahun.identityservice.domain.keyword.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KeywordCreateRequestDto {
    private String name;
}
