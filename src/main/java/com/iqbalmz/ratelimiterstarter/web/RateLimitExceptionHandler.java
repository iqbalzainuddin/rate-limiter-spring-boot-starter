package com.iqbalmz.ratelimiterstarter.web;

import com.iqbalmz.ratelimiterstarter.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Default handler that turns {@link RateLimitExceededException} into a 429 response.
 *
 * <p>Auto-configuration registers this only when no bean of this type exists,
 * so host applications can override the error shape.
 */
@RestControllerAdvice
public class RateLimitExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handle(RateLimitExceededException ex, HttpServletResponse response) {
        response.setHeader("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        Map<String, Object> body = Map.of(
                "status", 429,
                "error", "Too Many Requests",
                "message", "Rate limit exceeded"
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
}
