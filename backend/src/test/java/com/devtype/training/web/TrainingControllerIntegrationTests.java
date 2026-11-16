package com.devtype.training.web;

import com.devtype.training.domain.KeylogDecoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("TrainingController Integration Tests — Task 0.10 Vertical Slice")
class TrainingControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @Tag("AC-BE-1")
    @DisplayName("Vertical slice: POST /api/v1/sessions -> POST /api/v1/sessions/{id}/complete completes with official metrics")
    void testVerticalSliceEndToEnd() throws Exception {
        // Step 1: Start session
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

        // Step 2: Generate realistic keylog matching the exercise code
        WirePayload wire = buildWireKeylog(code);
        int elapsedMs = Math.max(0, (wire.count() - 1) * 60);

        // Step 3: Complete session
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
                .andExpect(jsonPath("$.metrics.accuracy").isNumber())
                .andReturn();

        JsonNode completeJson = objectMapper.readTree(completeResult.getResponse().getContentAsString());
        assertThat(completeJson.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(completeJson.get("metrics").get("wpm").asDouble()).isGreaterThan(0.0);
        assertThat(completeJson.get("metrics").get("accuracy").asDouble()).isEqualTo(100.0);
    }
}
