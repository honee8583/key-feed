package com.leedahun.identityservice.domain.source.service;

import com.leedahun.identityservice.domain.source.dto.SourceRequestDto;
import com.leedahun.identityservice.domain.source.dto.SourceResponseDto;
import java.util.List;

public interface SourceService {

    List<SourceResponseDto> getSourcesByUser(Long userId);

    SourceResponseDto addSource(Long userId, SourceRequestDto request);

    void removeUserSource(Long userId, Long userSourceId);

    List<SourceResponseDto> searchMySources(Long userId, String keyword);

}
