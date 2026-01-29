package com.leedahun.identityservice.domain.keyword.repository;

import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.keyword.dto.TrendingKeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.entity.Keyword;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findByUserId(Long userId);

    Optional<Keyword> findByIdAndUserId(Long keywordId, Long userId);

    boolean existsByNameAndUser(String name, User user);

    Long countByUserId(Long userId);

    @Query("SELECT DISTINCT k.user.id " +
            "FROM Keyword k " +
            "JOIN UserSource us ON k.user.id = us.user.id " +
            "WHERE k.name IN :keywords " +
            "AND us.source.id = :sourceId")
    List<Long> findUserIdsByNamesAndSourceId(@Param("keywords") Set<String> keywords, @Param("sourceId") Long sourceId);

    @Modifying
    @Query("DELETE FROM Keyword k WHERE k.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT k.name AS name, COUNT(k) AS count " +
            "FROM Keyword k " +
            "GROUP BY k.name " +
            "ORDER BY count DESC")
    List<TrendingKeywordResponseDto> findTrendingKeywords(Pageable pageable);

}
