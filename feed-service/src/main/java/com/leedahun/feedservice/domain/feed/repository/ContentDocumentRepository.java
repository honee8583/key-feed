package com.leedahun.feedservice.domain.feed.repository;

import com.leedahun.feedservice.domain.feed.document.ContentDocument;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ContentDocumentRepository extends ElasticsearchRepository<ContentDocument, String> {

    @Query("""
    {
      "bool": {
        "must": [
          {
            "terms": {
              "source_id": ?0
            }
          }
        ],
        "filter": [
          {
            "range": {
              "published_at": {
                "lt": "?1"
              }
            }
          }
        ]
      }
    }
    """)
    List<ContentDocument> searchBySourceIdsAndCursor(List<Long> sourceIds, String lastPublishedAt, Pageable pageable);

    @Query("""
    {
      "bool": {
        "must": [
          {
            "terms": {
              "source_id": ?0
            }
          }
        ]
      }
    }
    """)
    List<ContentDocument> searchBySourceIdsFirstPage(List<Long> sourceIds, Pageable pageable);

}