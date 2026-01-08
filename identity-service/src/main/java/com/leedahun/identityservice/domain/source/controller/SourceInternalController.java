package com.leedahun.identityservice.domain.source.controller;

import com.leedahun.identityservice.domain.source.dto.SourceResponseDto;
import com.leedahun.identityservice.domain.source.service.SourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/sources")
@RequiredArgsConstructor
public class SourceInternalController {

    private final SourceService sourceService;

    @GetMapping("/user/{userId}")
    public List<SourceResponseDto> getUserSources(@PathVariable Long userId) {
        return sourceService.getSourcesByUser(userId);
    }

}
