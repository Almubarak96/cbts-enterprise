
package com.almubaraksuleiman.cbts.api;

import com.almubaraksuleiman.cbts.security.authentication.service.TokenCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 * */

@RestController
@RequestMapping("/api/admin/exam")
@RequiredArgsConstructor
public class AdminExamController {

    private final TokenCleanupService tokenCleanupService;

    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cleanupTokens() {
        int deletedCount = tokenCleanupService.manualCleanup();
        return ResponseEntity.ok("Removed " + deletedCount + " expired/revoked refresh tokens");
    }

}

