package com.devtype.shared.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * RFC 9457 ProblemDetail obyektlarini standart formatda yaratish fabrikasi.
 */
public final class ProblemDetailFactory {

    private static final String BASE_PROBLEM_TYPE = "https://devtype.dev/problems/";

    private ProblemDetailFactory() {}

    public static ProblemDetail create(
        HttpStatus status,
        String code,
        String title,
        String detail,
        String traceId,
        List<ValidationError> errors,
        Map<String, Object> meta
    ) {
        String slug = code.toLowerCase().replace('_', '-');
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(BASE_PROBLEM_TYPE + slug));
        problem.setTitle(title);
        problem.setProperty("code", code);
        if (traceId != null) {
            problem.setProperty("traceId", traceId);
        }
        if (errors != null && !errors.isEmpty()) {
            problem.setProperty("errors", errors);
        }
        if (meta != null && !meta.isEmpty()) {
            problem.setProperty("meta", meta);
        }
        return problem;
    }

    public record ValidationError(String field, String code, String message) {}
}
