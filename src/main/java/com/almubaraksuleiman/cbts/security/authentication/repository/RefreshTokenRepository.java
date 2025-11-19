package com.almubaraksuleiman.cbts.security.authentication.repository;

import com.almubaraksuleiman.cbts.security.authentication.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUsernameAndRevokedFalse(String username);

    long countByUsernameAndRevokedFalse(String username);


    List<RefreshToken> findAllByUsernameAndRevokedFalse(String username);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiryDate < :now OR t.revoked = true")
    int deleteByExpiryDateBeforeOrRevokedTrue(Instant now);}
