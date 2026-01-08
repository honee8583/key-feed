package com.leedahun.identityservice.domain.auth.repository;

import com.leedahun.identityservice.domain.auth.entity.EmailPurpose;
import com.leedahun.identityservice.domain.auth.entity.EmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

    Optional<EmailVerification> findByEmailAndPurpose(String email, EmailPurpose purpose);

    Optional<EmailVerification> findTopByEmailAndPurposeOrderByIdDesc(String email, EmailPurpose purpose);

}
