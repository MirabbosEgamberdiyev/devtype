package com.devtype.analytics.web;

import com.devtype.identity.domain.Role;
import com.devtype.identity.domain.UserEntity;
import com.devtype.identity.domain.UserRepository;
import com.devtype.identity.domain.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Analytics & User Profile Integration Tests")
class AnalyticsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("analytics_test@example.com").orElseGet(() -> {
            UserEntity u = new UserEntity(
                    "analytics_test@example.com",
                    "analytics_user",
                    "$argon2id$v=19$m=19456,t=2,p=1$fakeHashForTests",
                    "uzl",
                    "Asia/Tashkent"
            );
            u.setRole(Role.USER);
            u.setStatus(UserStatus.ACTIVE);
            u.setEmailVerified(true);
            return userRepository.save(u);
        });
    }

    @Test
    @Tag("AC-AN-1")
    @DisplayName("GET /api/v1/analytics/overview returns KPI and languages summary")
    void testGetOverview() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/overview")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpi", notNullValue()))
                .andExpect(jsonPath("$.languages", isA(java.util.List.class)));
    }

    @Test
    @Tag("AC-AN-8")
    @DisplayName("GET /api/v1/analytics/languages/java with <5 sessions returns enoughData=false")
    void testGetLanguageDetailNotEnoughData() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/languages/java")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enoughData", is(false)))
                .andExpect(jsonPath("$.sessionsNeeded", greaterThanOrEqualTo(0)));
    }

    @Test
    @Tag("AC-AN-7")
    @DisplayName("GET /api/v1/analytics/activity returns 365-day calendar")
    void testGetActivityCalendar() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/activity?year=2026")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(365)))
                .andExpect(jsonPath("$[0].date", is("2026-01-01")))
                .andExpect(jsonPath("$[364].date", is("2026-12-31")));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/recommendations returns recommendation data")
    void testGetRecommendations() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/recommendations")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedMode", notNullValue()));
    }

    @Test
    @Tag("AC-UD-1")
    @DisplayName("GET /api/v1/me/dashboard returns complete dashboard payload")
    void testGetDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/me/dashboard")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gamification", notNullValue()))
                .andExpect(jsonPath("$.languages", notNullValue()))
                .andExpect(jsonPath("$.recentSessions", notNullValue()))
                .andExpect(jsonPath("$.isNewUser", isA(Boolean.class)));
    }

    @Test
    @Tag("AC-UM-11")
    @DisplayName("GET /api/v1/me/export returns all user data in JSON format")
    void testGetExport() throws Exception {
        mockMvc.perform(get("/api/v1/me/export")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exportedAt", notNullValue()))
                .andExpect(jsonPath("$.profile.email", is("analytics_test@example.com")));
    }

    @Test
    @Tag("AC-UM-11")
    @DisplayName("DELETE /api/v1/me schedules account deletion for 30 days")
    void testDeleteAccount() throws Exception {
        // Create dedicated user for deletion test with unique username/email
        String uniq = java.util.UUID.randomUUID().toString().substring(0, 8);
        UserEntity toDelete = userRepository.save(new UserEntity(
                "del_" + uniq + "@example.com",
                "del_" + uniq,
                "$argon2id$v=19$m=19456,t=2,p=1$fakeHashForTests",
                "en",
                "UTC"
        ));

        mockMvc.perform(delete("/api/v1/me")
                        .with(user(String.valueOf(toDelete.getId())).roles("USER")))
                .andExpect(status().isNoContent());

        UserEntity after = userRepository.findById(toDelete.getId()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(after.getDeletionScheduledAt()).isNotNull();
    }

    @Test
    @Tag("AC-UM-8")
    @DisplayName("GET and PATCH /api/v1/me user profile")
    void testGetAndPatchProfile() throws Exception {
        mockMvc.perform(get("/api/v1/me")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("analytics_test@example.com")));

        mockMvc.perform(patch("/api/v1/me")
                        .with(user(String.valueOf(testUser.getId())).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Analytics Pro\",\"bio\":\"Developer and typer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("Analytics Pro")))
                .andExpect(jsonPath("$.bio", is("Developer and typer")));
    }

    @Test
    @Tag("AC-UM-9")
    @DisplayName("GET and PUT /api/v1/me/settings persists JSON user settings")
    void testGetAndPutSettings() throws Exception {
        mockMvc.perform(put("/api/v1/me/settings")
                        .with(user(String.valueOf(testUser.getId())).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"dark\",\"fontSize\":16}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme", is("dark")))
                .andExpect(jsonPath("$.fontSize", is(16)));

        mockMvc.perform(get("/api/v1/me/settings")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme", is("dark")))
                .andExpect(jsonPath("$.fontSize", is(16)));
    }

    @Test
    @Tag("AC-AN-9")
    @DisplayName("GET /api/v1/sessions returns session history")
    void testGetSessionsHistory() throws Exception {
        mockMvc.perform(get("/api/v1/sessions?limit=10")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }
}
