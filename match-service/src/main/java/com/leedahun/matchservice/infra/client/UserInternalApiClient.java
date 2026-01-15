package com.leedahun.matchservice.infra.client;

import com.leedahun.matchservice.infra.client.dto.KeywordResponseDto;
import java.util.List;
import java.util.Set;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "identity-service")
public interface UserInternalApiClient {

    @GetMapping("/internal/users/{userId}/keywords")
    List<KeywordResponseDto> getActiveKeywords(@PathVariable("userId") Long userId);

    @PostMapping("/internal/keywords/match-users")
    List<Long> findUserIdsByKeywordsAndSource(@RequestBody Set<String> keywords,
                                              @RequestParam("sourceId") Long sourceId);

}
