package com.devtype.shared.error;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

/**
 * Domen qatlamidagi biznes qoidalari buzilganda tashlanadigan istisno.
 */
public class DomainException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final Map<String, Object> details;

    public DomainException(String code, String message, HttpStatus status) {
        this(code, message, status, Collections.emptyMap());
    }

    public DomainException(String code, String message, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details != null ? details : Collections.emptyMap();
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
