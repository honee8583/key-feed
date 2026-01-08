package com.leedahun.feedservice.domain.feed.repository;

import com.leedahun.feedservice.domain.feed.entity.Content;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query(value = """
                SELECT *
                FROM content
                WHERE (title REGEXP :pattern OR summary REGEXP :pattern)
                  AND (:cursorId IS NULL OR content_id < :cursorId)
                ORDER BY content_id DESC
                LIMIT :limit
            """, nativeQuery = true)
    List<Content> searchByKeywordsKeyset(
            @Param("pattern") String pattern,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit
    );

}
