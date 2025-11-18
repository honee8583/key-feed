package com.leedahun.identityservice.domain.keyword.repository;

import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.keyword.entity.Keyword;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findByUserId(Long userId);

    Optional<Keyword> findByIdAndUserId(Long keywordId, Long userId);

    boolean existsByNameAndUser(String name, User user);

    Long countByUserId(Long userId);

}
