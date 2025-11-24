package com.leedahun.identityservice.domain.source.repository;

import com.leedahun.identityservice.domain.source.entity.UserSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSourceRepository extends JpaRepository<UserSource, Long> {

    List<UserSource> findByUserId(Long userId);

    Optional<UserSource> findByIdAndUserId(Long userSourceId, Long userId);

    boolean existsByUserIdAndSourceId(Long userId, Long sourceId);

}
