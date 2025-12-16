package com.leedahun.identityservice.domain.bookmark.repository;

import com.leedahun.identityservice.domain.bookmark.entity.BookmarkFolder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkFolderRepository extends JpaRepository<BookmarkFolder, Long> {

    // 폴더명 중복 검사
    boolean existsByUserIdAndName(Long userId, String name);

    // 사용자의 북마크 폴더 개수
    long countByUserId(Long userId);

}
