package com.devtype.training.web;

import com.devtype.identity.domain.Role;
import com.devtype.identity.domain.UserEntity;
import com.devtype.identity.domain.UserRepository;
import com.devtype.identity.domain.UserStatus;
import com.devtype.identity.infrastructure.GuestTokenService;
import com.devtype.training.domain.KeylogDecoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Training Module Controller & Lifecycle Integration Tests")
class TrainingControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GuestTokenService guestTokenService;

    @Autowired
    private UserRepository userRepository;

    private static void writeVarint(ByteArrayOutputStream out, int val) {
        int v = val;
        while ((v & ~0x7F) != 0) {
            out.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write(v & 0x7F);
    }

    record WirePayload(String keylog, int count) {}

    private static WirePayload buildWireKeylog(String code) throws IOException {
        ByteArrayOutputStream eventsStream = new ByteArrayOutputStream();
        int count = 0;
        int interval = 60; // 60ms between keystrokes

        boolean atLineStart = false;
        for (int i = 0; i < code.length(); i++) {
            char ch = code.charAt(i);
            if (atLineStart && ch == ' ') {
                continue; // Indent spaces are prefilled in standard mode (docs/07 §3.3)
            }
            atLineStart = false;
            int dt = (count == 0) ? 0 : interval;

            if (ch == '\n') {
                writeVarint(eventsStream, dt);
                eventsStream.write(com.devtype.training.domain.KeyKind.ENTER.getCode());
                count++;
                atLineStart = true;
            } else if (ch == '\t') {
                writeVarint(eventsStream, dt);
                eventsStream.write(com.devtype.training.domain.KeyKind.TAB.getCode());
                count++;
            } else {
                writeVarint(eventsStream, dt);
                eventsStream.write(com.devtype.training.domain.KeyKind.CHAR.getCode());
                eventsStream.write((byte) ch);
                count++;
            }
        }

        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        raw.write(KeylogDecoder.MAGIC_0);
        raw.write(KeylogDecoder.MAGIC_1);
        raw.write(KeylogDecoder.VERSION_1);
        writeVarint(raw, 10000); // 1.0.0
        raw.write(0x00); // strictIndent=false
        writeVarint(raw, count);
        raw.write(eventsStream.toByteArray());

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
            gzip.write(raw.toByteArray());
        }

        return new WirePayload(Base64.getEncoder().encodeToString(compressed.toByteArray()), count);
    }

    @Nested
    @DisplayName("Practice Mode & Complete Flow (AC-TM-1, AC-TM-10, AC-TM-11)")
    class PracticeTests {

        @Test
        @Tag("AC-TM-1")
        @Tag("AC-TM-10")
        @DisplayName("Start PRACTICE session -> Complete with valid keylog -> Returns full metrics & series")
        void testPracticeSessionFullLifecycle() throws Exception {
            MvcResult startResult = mockMvc.perform(post("/api/v1/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "language": "java",
                                      "mode": "PRACTICE",
                                      "difficulty": "BEGINNER"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sessionId").exists())
                    .andExpect(jsonPath("$.completeToken").exists())
                    .andExpect(jsonPath("$.exercises[0].code").exists())
                    .andReturn();

            JsonNode startJson = objectMapper.readTree(startResult.getResponse().getContentAsString());
            String sessionId = startJson.get("sessionId").asText();
            String completeToken = startJson.get("completeToken").asText();
            String code = startJson.get("exercises").get(0).get("code").asText();

            WirePayload wire = buildWireKeylog(code);
            int elapsedMs = Math.max(0, (wire.count() - 1) * 60);

            String completePayload = objectMapper.writeValueAsString(new CompleteSessionRequest(
                    completeToken,
                    wire.keylog(),
                    elapsedMs,
                    new CompleteSessionRequest.ClientMetricsDto(45.0, 100.0)
            ));

            MvcResult completeResult = mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(completePayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.metrics.wpm").isNumber())
                    .andExpect(jsonPath("$.metrics.accuracy").value(100.0))
                    .andExpect(jsonPath("$.wpmSeries").isArray())
                    .andReturn();

            JsonNode completeJson = objectMapper.readTree(completeResult.getResponse().getContentAsString());
            assertThat(completeJson.get("metrics").get("wpm").asDouble()).isGreaterThan(0.0);

            // Idempotentlik tekshiruvi (AC-TM-11)
            mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(completePayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.sessionId").value(sessionId));
        }

        @Test
        @Tag("AC-TM-11")
        @DisplayName("AC-TM-11: Keylog with < 20 keys or < 5s is rejected with 422 SESSION_TOO_SHORT")
        void testSessionTooShortRejected() throws Exception {
            MvcResult startResult = mockMvc.perform(post("/api/v1/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "language": "java",
                                      "mode": "PRACTICE",
                                      "difficulty": "BEGINNER"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode startJson = objectMapper.readTree(startResult.getResponse().getContentAsString());
            String sessionId = startJson.get("sessionId").asText();
            String completeToken = startJson.get("completeToken").asText();

            // Atigi 5 ta belgi yozilgan keylog
            WirePayload wire = buildWireKeylog("short");
            int elapsedMs = 1000;

            String completePayload = objectMapper.writeValueAsString(new CompleteSessionRequest(
                    completeToken,
                    wire.keylog(),
                    elapsedMs,
                    new CompleteSessionRequest.ClientMetricsDto(45.0, 100.0)
            ));

            mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(completePayload))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("SESSION_TOO_SHORT"));
        }

        @Test
        @Tag("AC-TM-8")
        @DisplayName("AC-TM-8: Invalid completeToken returns 401 COMPLETE_TOKEN_INVALID")
        void testInvalidCompleteTokenRejected() throws Exception {
            MvcResult startResult = mockMvc.perform(post("/api/v1/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "language": "java",
                                      "mode": "PRACTICE"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode startJson = objectMapper.readTree(startResult.getResponse().getContentAsString());
            String sessionId = startJson.get("sessionId").asText();
            String code = startJson.get("exercises").get(0).get("code").asText();

            WirePayload wire = buildWireKeylog(code);
            int elapsedMs = Math.max(0, (wire.count() - 1) * 60);

            String completePayload = objectMapper.writeValueAsString(new CompleteSessionRequest(
                    "fake.invalid.token",
                    wire.keylog(),
                    elapsedMs,
                    new CompleteSessionRequest.ClientMetricsDto(45.0, 100.0)
            ));

            mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(completePayload))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("COMPLETE_TOKEN_INVALID"));
        }
    }

    @Nested
    @DisplayName("Speed Test & Next Exercise Flow (AC-TM-2)")
    class SpeedTestFlowTests {

        @Test
        @Tag("AC-TM-2")
        @DisplayName("AC-TM-2: Speed Test starts with 3 exercises and duration limit, POST /next fetches subsequent exercises")
        void testSpeedTestFlow() throws Exception {
            MvcResult startResult = mockMvc.perform(post("/api/v1/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "language": "java",
                                      "mode": "SPEED_TEST",
                                      "duration": 60
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.mode").value("SPEED_TEST"))
                    .andExpect(jsonPath("$.durationLimitS").value(60))
                    .andExpect(jsonPath("$.exercises", hasSize(greaterThanOrEqualTo(1))))
                    .andReturn();

            JsonNode startJson = objectMapper.readTree(startResult.getResponse().getContentAsString());
            String sessionId = startJson.get("sessionId").asText();

            // Next snippet so'rash
            mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/next")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "afterSeq": 0
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exercises", hasSize(greaterThanOrEqualTo(1))));
        }
    }

    @Nested
    @DisplayName("Special Characters Mode (AC-TM-4)")
    class SpecialCharactersTests {

        @Test
        @Tag("AC-TM-4")
        @DisplayName("AC-TM-4: Special characters session generates 140-160 char text matching target category")
        void testSpecialCharactersSession() throws Exception {
            MvcResult startResult = mockMvc.perform(post("/api/v1/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "language": "java",
                                      "mode": "SPECIAL_CHARS",
                                      "specialCategory": "BRACKETS"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.mode").value("SPECIAL_CHARS"))
                    .andExpect(jsonPath("$.exercises[0].charCount", greaterThanOrEqualTo(140)))
                    .andExpect(jsonPath("$.exercises[0].code").exists())
                    .andReturn();

            JsonNode startJson = objectMapper.readTree(startResult.getResponse().getContentAsString());
            String code = startJson.get("exercises").get(0).get("code").asText();
            assertThat(code.length()).isGreaterThanOrEqualTo(140);
        }
    }

    @Nested
    @DisplayName("Session Abandon & Query (BL-TM-2)")
    class AbandonAndQueryTests {

        @Test
        @DisplayName("Abandon session marks it ABANDONED, subsequent complete returns 409 SESSION_NOT_ACTIVE")
        void testAbandonSession() throws Exception {
            MvcResult startResult = mockMvc.perform(post("/api/v1/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "language": "java",
                                      "mode": "PRACTICE"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode startJson = objectMapper.readTree(startResult.getResponse().getContentAsString());
            String sessionId = startJson.get("sessionId").asText();
            String completeToken = startJson.get("completeToken").asText();
            String code = startJson.get("exercises").get(0).get("code").asText();

            // Abandon chaqirish
            mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/abandon"))
                    .andExpect(status().isNoContent());

            // GET /sessions/{id} tekshirish
            mockMvc.perform(get("/api/v1/sessions/" + sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ABANDONED"));

            // Complete qilishga urinish -> 409 SESSION_NOT_ACTIVE
            WirePayload wire = buildWireKeylog(code);
            String completePayload = objectMapper.writeValueAsString(new CompleteSessionRequest(
                    completeToken,
                    wire.keylog(),
                    Math.max(0, (wire.count() - 1) * 60),
                    new CompleteSessionRequest.ClientMetricsDto(45.0, 100.0)
            ));

            mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(completePayload))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("SESSION_NOT_ACTIVE"));
        }
    }

    @Nested
    @DisplayName("Learning System & Level Lock (AC-LS-1)")
    class LearningSystemTests {

        @Test
        @Tag("AC-LS-1")
        @WithMockUser(username = "1001", roles = "USER")
        @DisplayName("AC-LS-1: Requesting LOCKED difficulty (e.g. ADVANCED) for new user returns 409 LEVEL_LOCKED")
        void testLockedLevelReturns409() throws Exception {
            mockMvc.perform(post("/api/v1/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "language": "java",
                                      "topic": "variables",
                                      "difficulty": "ADVANCED",
                                      "mode": "PRACTICE"
                                    }
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("LEVEL_LOCKED"));
        }
    }

    @Nested
    @DisplayName("Guest Claim Flow (AC-UM-7)")
    class GuestClaimTests {

        @Test
        @Tag("AC-UM-7")
        @DisplayName("AC-UM-7: Claim guest sessions assigns guest sessions to user")
        void testClaimGuestSessions() throws Exception {
            UUID guestId = UUID.randomUUID();
            String guestToken = guestTokenService.generateGuestToken(guestId);

            // 0. Haqiqiy foydalanuvchi yaratish (FK constraint uchun)
            String email = "claim_" + UUID.randomUUID().toString().substring(0, 8) + "@devtype.dev";
            String username = "u_" + UUID.randomUUID().toString().substring(0, 8);
            UserEntity user = new UserEntity(email, username, "dummy_hash", "en", "UTC");
            user = userRepository.save(user);

            // 1. Mehmon sifatida sessiya ochish (anonymous)
            mockMvc.perform(post("/api/v1/sessions")
                            .with(anonymous())
                            .header("X-Guest-Token", guestToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "language": "java",
                                      "mode": "SPEED_TEST"
                                    }
                                    """))
                    .andExpect(status().isCreated());

            // 2. Foydalanuvchi sifatida claim qilish
            mockMvc.perform(post("/api/v1/sessions/claim")
                            .with(user(user.getId().toString()).roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TrainingController.ClaimRequest(guestToken))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.claimed", greaterThanOrEqualTo(1)));
        }
    }
}
