package com.almubaraksuleiman.cbts.security.authentication.service;

import com.almubaraksuleiman.cbts.security.authentication.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    // Runs every day at 02:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledCleanup() {
        int deletedCount = cleanup();
        if (deletedCount > 0) {
            log.info("Scheduled cleanup: removed {} expired/revoked refresh tokens", deletedCount);
        }
    }

    // Public method for manual trigger
    public int manualCleanup() {
        int deletedCount = cleanup();
        log.info("Manual cleanup: removed {} expired/revoked refresh tokens", deletedCount);
        return deletedCount;
    }

    private int cleanup() {
        return refreshTokenRepository.deleteByExpiryDateBeforeOrRevokedTrue(Instant.now());
    }
}
