package com.leedahun.matchservice.domain.content.repository;

import com.leedahun.matchservice.domain.content.document.ContentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ContentDocumentRepository extends ElasticsearchRepository<ContentDocument, String> {

}