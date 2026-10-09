package com.portfolio.scheduler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(TaskNotFoundException.class)
    ResponseEntity<Map<String, Object>> notFound(TaskNotFoundException e) { return response(HttpStatus.NOT_FOUND, e.getMessage()); }
    @ExceptionHandler({InvalidTaskStateException.class, IllegalStateException.class})
    ResponseEntity<Map<String, Object>> conflict(RuntimeException e) { return response(HttpStatus.CONFLICT, e.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) { return response(HttpStatus.BAD_REQUEST, e.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst().map(x -> x.getField() + " " + x.getDefaultMessage()).orElse("Invalid request");
        return response(HttpStatus.BAD_REQUEST, message);
    }
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> malformed(Exception e) { return response(HttpStatus.BAD_REQUEST, "Malformed request body or unsupported enum value"); }
    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now(), "status", status.value(), "error", status.getReasonPhrase(), "message", message));
    }
}
