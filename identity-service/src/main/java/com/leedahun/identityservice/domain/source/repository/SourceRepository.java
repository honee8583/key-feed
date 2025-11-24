package com.leedahun.identityservice.domain.source.repository;

import com.leedahun.identityservice.domain.source.entity.Source;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRepository extends JpaRepository<Source, Long> {

    Optional<Source> findByUrl(String url);

}
