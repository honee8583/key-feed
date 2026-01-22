package com.leedahun.identityservice.domain.source.repository;

import com.leedahun.identityservice.domain.source.entity.UserSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSourceRepository extends JpaRepository<UserSource, Long> {

    List<UserSource> findByUserId(Long userId);

    Optional<UserSource> findByIdAndUserId(Long userSourceId, Long userId);

    boolean existsByUserIdAndSourceId(Long userId, Long sourceId);

    @Query("SELECT us FROM UserSource us JOIN FETCH us.source s " +
            "WHERE us.user.id = :userId " +
            "AND (LOWER(us.userDefinedName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<UserSource> searchByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

}
