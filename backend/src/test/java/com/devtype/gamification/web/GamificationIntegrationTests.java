package com.devtype.gamification.web;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Gamification Integration Tests")
class GamificationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("gamify_test@example.com").orElseGet(() -> {
            UserEntity u = new UserEntity(
                    "gamify_test@example.com",
                    "gamify_user",
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
    @Tag("AC-GM-4")
    @DisplayName("GET /api/v1/gamification/me returns valid user summary and progression")
    void testGetGamificationMe() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/me")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalXp", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.streak.current", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.streak.freezesLeft", greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("GET /api/v1/gamification/me unauthenticated returns 401")
    void testGetGamificationMeUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("AC-GM-3")
    @DisplayName("GET /api/v1/gamification/xp-history returns ledger list")
    void testGetXpHistory() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/xp-history")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @Tag("AC-GM-7")
    @DisplayName("GET /api/v1/records returns personal records list")
    void testGetPersonalRecords() throws Exception {
        mockMvc.perform(get("/api/v1/records")
                        .with(user(String.valueOf(testUser.getId())).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }
}
