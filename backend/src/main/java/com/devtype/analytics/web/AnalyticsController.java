package com.devtype.analytics.web;

import com.devtype.analytics.application.AnalyticsDtos;
import com.devtype.analytics.application.AnalyticsQueryService;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    public AnalyticsController(AnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/overview")
    public ResponseEntity<AnalyticsDtos.OverviewResponse> getOverview(
            @RequestParam(name = "group", defaultValue = "CORE") String group,
            Authentication auth
    ) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(analyticsQueryService.getOverview(userId, group));
    }

    @GetMapping("/languages/{slug}")
    public ResponseEntity<AnalyticsDtos.LanguageDetailResponse> getLanguageAnalytics(
            @PathVariable("slug") String slug,
            @RequestParam(name = "period", defaultValue = "30d") String period,
            @RequestParam(name = "bucket", defaultValue = "day") String bucket,
            @RequestParam(name = "group", defaultValue = "CORE") String group,
            Authentication auth
    ) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(analyticsQueryService.getLanguageAnalytics(userId, slug, period, bucket, group));
    }

    @GetMapping("/languages/{slug}/chars")
    public ResponseEntity<List<AnalyticsDtos.CharStatItemDto>> getCharAnalytics(
            @PathVariable("slug") String slug,
            Authentication auth
    ) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(analyticsQueryService.getCharAnalytics(userId, slug));
    }

    @GetMapping("/activity")
    public ResponseEntity<List<AnalyticsDtos.ActivityDayDto>> getActivityCalendar(
            @RequestParam(name = "year", required = false) Integer year,
            Authentication auth
    ) {
        Long userId = requireUserId(auth);
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(analyticsQueryService.getActivityCalendar(userId, targetYear));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<AnalyticsDtos.RecommendationsResponse> getRecommendations(Authentication auth) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(analyticsQueryService.getRecommendations(userId));
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
