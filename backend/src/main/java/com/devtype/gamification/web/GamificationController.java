package com.devtype.gamification.web;

import com.devtype.gamification.application.GamificationDtos;
import com.devtype.gamification.application.GamificationService;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/gamification/me")
    public ResponseEntity<GamificationDtos.GamificationMeResponse> getGamificationMe(Authentication auth) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(gamificationService.getGamificationMe(userId));
    }

    @GetMapping("/gamification/xp-history")
    public ResponseEntity<List<GamificationDtos.XpTransactionDto>> getXpHistory(
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            Authentication auth
    ) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(gamificationService.getXpHistory(userId, limit));
    }

    @GetMapping("/records")
    public ResponseEntity<List<GamificationDtos.PersonalRecordItemDto>> getPersonalRecords(Authentication auth) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(gamificationService.getPersonalRecords(userId));
    }

    private Long requireUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new DomainException("UNAUTHORIZED", "Avtorizatsiya talab qilinadi", HttpStatus.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new DomainException("UNAUTHORIZED", "Yaroqsiz foydalanuvchi identifikatori", HttpStatus.UNAUTHORIZED);
        }
    }
}
