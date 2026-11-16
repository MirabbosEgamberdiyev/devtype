package com.devtype.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex, HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        log.warn("Domain exception [{}]: {} (traceId: {})", ex.getCode(), ex.getMessage(), traceId);

        ProblemDetail problem = ProblemDetailFactory.create(
            ex.getStatus(),
            ex.getCode(),
            ex.getMessage(),
            ex.getMessage(),
            traceId,
            null,
            ex.getDetails()
        );
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        List<ProblemDetailFactory.ValidationError> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ProblemDetailFactory.ValidationError(
                fieldError.getField(),
                fieldError.getCode() != null ? fieldError.getCode() : "INVALID",
                fieldError.getDefaultMessage()
            ));
        }

        ProblemDetail problem = ProblemDetailFactory.create(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_FAILED",
            "Validation Failed",
            "Request validation failed for one or more fields",
            traceId,
            errors,
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        log.error("Unhandled exception occurred (traceId: {})", traceId, ex);

        ProblemDetail problem = ProblemDetailFactory.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "Internal Server Error",
            "An unexpected error occurred. Please contact support with trace ID.",
            traceId,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private String getOrCreateTraceId(HttpServletRequest request) {
        String traceId = (String) request.getAttribute("traceId");
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        String headerTraceId = request.getHeader("X-Trace-Id");
        if (headerTraceId != null && !headerTraceId.isBlank()) {
            return headerTraceId;
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
