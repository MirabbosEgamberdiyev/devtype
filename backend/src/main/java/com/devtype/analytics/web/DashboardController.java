package com.devtype.analytics.web;

import com.devtype.analytics.application.DashboardDtos;
import com.devtype.analytics.application.DashboardService;
import com.devtype.analytics.application.UserDataExportService;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/me")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserDataExportService userDataExportService;

    public DashboardController(
            DashboardService dashboardService,
            UserDataExportService userDataExportService
    ) {
        this.dashboardService = dashboardService;
        this.userDataExportService = userDataExportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDtos.DashboardResponse> getDashboard(Authentication auth) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(dashboardService.getDashboard(userId));
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportUserData(Authentication auth) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(userDataExportService.exportUserData(userId));
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
