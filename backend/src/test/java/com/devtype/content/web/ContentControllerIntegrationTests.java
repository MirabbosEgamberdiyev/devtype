package com.devtype.content.web;

import com.devtype.content.domain.DifficultyLevel;
import com.devtype.content.presentation.ContentDtos.CreateExerciseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Content Module Controller Integration Tests")
class ContentControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Languages Catalog Tests (AC-PL-1, AC-PL-2)")
    class LanguageCatalogTests {

        @Test
        @Tag("AC-PL-1")
        @DisplayName("AC-PL-1: GET /api/v1/languages returns published and draft languages in release order")
        void getLanguagesReturnsPublishedAndDraft() throws Exception {
            mockMvc.perform(get("/api/v1/languages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                    .andExpect(jsonPath("$[?(@.slug == 'java')].status").value("PUBLISHED"))
                    .andExpect(jsonPath("$[?(@.slug == 'sql')].status").value("PUBLISHED"))
                    .andExpect(jsonPath("$[?(@.slug == 'plsql')].status").value("PUBLISHED"))
                    .andExpect(jsonPath("$[?(@.slug == 'typescript')].status").value("DRAFT"));
        }

        @Test
        @Tag("AC-PL-2")
        @WithMockUser(roles = "ADMIN")
        @DisplayName("AC-PL-2: Publishing an unready language without topics returns 409 LANGUAGE_NOT_READY")
        void publishingUnreadyLanguageFails() throws Exception {
            mockMvc.perform(post("/api/v1/admin/languages/typescript/publish"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("LANGUAGE_NOT_READY"));
        }
    }

    @Nested
    @DisplayName("Exercise Management & Validation Tests (AC-LS-7, BL-EM-1..4, AC-PLS-2)")
    class ExerciseValidationTests {

        @Test
        @Tag("AC-LS-7")
        @WithMockUser(roles = "EDITOR")
        @DisplayName("AC-LS-7: Valid exercise creation returns 201 and DRAFT status; duplicate returns 409")
        void validExerciseLifecycleAndDuplicateDetection() throws Exception {
            String uniqueVar = "v_" + UUID.randomUUID().toString().substring(0, 8);
            String validJavaCode = "public class " + uniqueVar + " {\n    private int count = 0;\n}";

            CreateExerciseRequest request = new CreateExerciseRequest(
                    "java",
                    null,
                    DifficultyLevel.BEGINNER,
                    "Basic Class Declaration",
                    "Demonstrates simple class structure",
                    validJavaCode,
                    false
            );

            // 1. Create exercise succeeds (201 CREATED)
            MvcResult createResult = mockMvc.perform(post("/api/v1/admin/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.charCount").isNumber())
                    .andExpect(jsonPath("$.exerciseKey").isNotEmpty())
                    .andReturn();

            // 2. Duplicate exact code in same language returns 409 EXERCISE_DUPLICATE (BL-EM-4)
            mockMvc.perform(post("/api/v1/admin/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EXERCISE_DUPLICATE"));
        }

        @Test
        @Tag("AC-PLS-2")
        @WithMockUser(roles = "EDITOR")
        @DisplayName("AC-PLS-2: PL/SQL exercise containing forbidden syntax ($$, LANGUAGE plpgsql) is rejected")
        void plsqlForbiddenPatternsRejected() throws Exception {
            String forbiddenPlsqlCode = "CREATE OR REPLACE FUNCTION test_fn()\n" +
                    "RETURNS void AS $$\n" +
                    "BEGIN\n" +
                    "    RAISE NOTICE 'Invalid dialekt';\n" +
                    "END; $$ LANGUAGE plpgsql;";

            CreateExerciseRequest request = new CreateExerciseRequest(
                    "plsql",
                    null,
                    DifficultyLevel.INTERMEDIATE,
                    "Invalid PostgreSQL Code in PL/SQL",
                    null,
                    forbiddenPlsqlCode,
                    false
            );

            mockMvc.perform(post("/api/v1/admin/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN_PATTERN"));
        }

        @Test
        @WithMockUser(roles = "EDITOR")
        @DisplayName("BL-EM-1: Line exceeding 80 characters returns 400 LINE_TOO_LONG")
        void lineTooLongRejected() throws Exception {
            String longLine = "// " + "A".repeat(85);
            String code = "public class TooLong {\n" + longLine + "\n}";

            CreateExerciseRequest request = new CreateExerciseRequest(
                    "java",
                    null,
                    DifficultyLevel.INTERMEDIATE,
                    "Long Line Exercise",
                    null,
                    code,
                    false
            );

            mockMvc.perform(post("/api/v1/admin/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("LINE_TOO_LONG"));
        }
    }
}
