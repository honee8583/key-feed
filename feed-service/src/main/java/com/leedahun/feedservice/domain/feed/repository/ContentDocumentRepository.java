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
            "multi_match": {
              "query": "?0",
              "fields": ["title", "summary"],
              "operator": "or"
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
    List<ContentDocument> searchByKeywordsAndCursor(String keywords, String lastPublishedAt, Pageable pageable);

    @Query("""
    {
      "bool": {
        "must": [
          {
            "multi_match": {
              "query": "?0",
              "fields": ["title", "summary"],
              "operator": "or"
            }
          }
        ]
      }
    }
    """)
    List<ContentDocument> searchByKeywordsFirstPage(String keywords, Pageable pageable);

}