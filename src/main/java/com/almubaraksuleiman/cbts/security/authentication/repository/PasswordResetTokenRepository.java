package com.almubaraksuleiman.cbts.security.authentication.repository;

import com.almubaraksuleiman.cbts.security.authentication.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByEmailAndUsedFalse(String email);
    
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.email = ?1 AND prt.used = false")
    void invalidateAllTokensForEmail(String email);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}