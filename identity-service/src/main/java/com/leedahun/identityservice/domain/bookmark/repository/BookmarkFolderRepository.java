package com.leedahun.identityservice.domain.bookmark.repository;

import com.leedahun.identityservice.domain.bookmark.entity.BookmarkFolder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkFolderRepository extends JpaRepository<BookmarkFolder, Long> {

    // 내 북마크 폴더 목록
    List<BookmarkFolder> findAllByUserIdOrderById(Long userId);

    // 폴더명 중복 검사
    boolean existsByUserIdAndName(Long userId, String name);

    // 사용자의 북마크 폴더 개수
    long countByUserId(Long userId);

    void deleteAllByUserId(Long userId);

}
