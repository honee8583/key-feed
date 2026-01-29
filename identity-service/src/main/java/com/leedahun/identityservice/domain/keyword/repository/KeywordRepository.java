package com.leedahun.identityservice.domain.keyword.repository;

import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.keyword.dto.TrendingKeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.entity.Keyword;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findByUserId(Long userId);

    Optional<Keyword> findByIdAndUserId(Long keywordId, Long userId);

    boolean existsByNameAndUser(String name, User user);

    Long countByUserId(Long userId);

    @Query("SELECT DISTINCT k.user.id FROM Keyword k WHERE k.name IN :keywords")
    List<Long> findUserIdsByNames(@Param("keywords") Set<String> keywords);

    @Query("SELECT k.name AS name, COUNT(k) AS count " +
            "FROM Keyword k " +
            "GROUP BY k.name " +
            "ORDER BY count DESC")
    List<TrendingKeywordResponseDto> findTrendingKeywords(Pageable pageable);

}
