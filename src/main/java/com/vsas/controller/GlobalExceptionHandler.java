package com.vsas.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vsas.exception.ConflictFieldException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictFieldException.class)
    public ResponseEntity<Map<String, Object>> conflictField(ConflictFieldException ex) {
        String field = ex.getField() != null ? ex.getField() : "_form";
        String msg = ex.getMessage() != null ? ex.getMessage() : "Conflict";
        Map<String, String> fieldErrors = Map.of(field, msg);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", labelForField(field) + ": " + msg);
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage(), "fieldErrors", Map.of()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> unauthorized(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        Map.of(
                                "error",
                                "Sign-in failed: username or password is wrong. Check caps lock and try again.",
                                "fieldErrors",
                                Map.of()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> forbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                        Map.of(
                                "error",
                                "You are not allowed to do this. Guests cannot upload or download scrolls; some actions require ADMIN.",
                                "fieldErrors",
                                Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ObjectError oe : ex.getBindingResult().getAllErrors()) {
            if (oe instanceof FieldError fe) {
                fieldErrors.merge(
                        fe.getField(),
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a + " Also: " + b);
            } else {
                fieldErrors.merge(
                        "_form",
                        oe.getDefaultMessage() != null ? oe.getDefaultMessage() : "Invalid request",
                        (a, b) -> a + " " + b);
            }
        }
        String summary =
                fieldErrors.entrySet().stream()
                        .map(e -> labelForField(e.getKey()) + ": " + e.getValue())
                        .collect(Collectors.joining(" · "));
        String error =
                fieldErrors.size() == 1
                        ? summary
                        : "Please correct the following (" + fieldErrors.size() + " issues): " + summary;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private static String labelForField(String field) {
        if (field == null || field.isBlank()) {
            return "Form";
        }
        return switch (field) {
            case "username" -> "Username";
            case "password" -> "Password";
            case "currentPassword" -> "Current password";
            case "newPassword" -> "New password";
            case "idKey" -> "ID key";
            case "fullName" -> "Full name";
            case "email" -> "Email";
            case "phoneNumber" -> "Phone number";
            case "role" -> "Role";
            case "scrollId" -> "Scroll ID";
            case "name" -> "Name";
            case "title" -> "Title";
            case "content" -> "Content";
            case "_form" -> "Form";
            default -> field;
        };
    }
}
