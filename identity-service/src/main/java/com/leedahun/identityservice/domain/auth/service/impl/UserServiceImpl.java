package com.leedahun.identityservice.domain.auth.service.impl;

import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.domain.auth.dto.PasswordChangeRequestDto;
import com.leedahun.identityservice.domain.auth.dto.WithdrawRequestDto;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.exception.InvalidPasswordException;
import com.leedahun.identityservice.domain.auth.exception.PasswordMismatchException;
import com.leedahun.identityservice.domain.auth.exception.SamePasswordException;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.auth.service.UserService;
import com.leedahun.identityservice.domain.bookmark.repository.BookmarkFolderRepository;
import com.leedahun.identityservice.domain.bookmark.repository.BookmarkRepository;
import com.leedahun.identityservice.domain.keyword.repository.KeywordRepository;
import com.leedahun.identityservice.domain.source.repository.UserSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final KeywordRepository keywordRepository;
    private final UserSourceRepository userSourceRepository;
    private final BookmarkFolderRepository bookmarkFolderRepository;
    private final BookmarkRepository bookmarkRepository;

    @Override
    public void changePassword(Long userId, PasswordChangeRequestDto requestDto) {
        User user = resolveUser(userId);

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }

        user.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }

    @Override
    public void withdraw(Long userId, WithdrawRequestDto requestDto) {
        User user = resolveUser(userId);

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        bookmarkRepository.deleteAllByUserId(userId);
        bookmarkFolderRepository.deleteAllByUserId(userId);
        userSourceRepository.deleteAllByUserId(userId);
        keywordRepository.deleteAllByUserId(userId);

        userRepository.delete(user);
    }

    private User resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", String.valueOf(userId)));
    }

}
