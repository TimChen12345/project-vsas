package com.vsas.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vsas.exception.ConflictFieldException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(new DummyController())
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void conflictField_returnsFieldErrors() throws Exception {
        mockMvc.perform(get("/dummy/conflict"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.fieldErrors.username").exists());
    }

    @Test
    void validation_returnsMultipleFieldErrors() throws Exception {
        mockMvc.perform(
                        post("/dummy/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.value").exists());
    }

    @Test
    void validation_twoFields_usesPluralSummary() throws Exception {
        mockMvc.perform(
                        post("/dummy/validate-two")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"a\":\"\",\"b\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("2 issues")))
                .andExpect(jsonPath("$.fieldErrors.a").exists())
                .andExpect(jsonPath("$.fieldErrors.b").exists());
    }

    @Test
    void illegalArgument_returnsPlainError() throws Exception {
        mockMvc.perform(get("/dummy/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("nope"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void badCredentials_returns401() throws Exception {
        mockMvc.perform(get("/dummy/badcred"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void accessDenied_returns403() throws Exception {
        mockMvc.perform(get("/dummy/denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void conflictField_nullFieldUsesFormLabel() throws Exception {
        mockMvc.perform(get("/dummy/conflict-null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors._form").exists());
    }

    @RestController
    @RequestMapping("/dummy")
    static class DummyController {

        @GetMapping("/conflict")
        void conflict() {
            throw new ConflictFieldException("username", "taken");
        }

        @PostMapping("/validate")
        void validate(@Valid @RequestBody ValidDto dto) {}

        @PostMapping("/validate-two")
        void validateTwo(@Valid @RequestBody TwoFields dto) {}

        @GetMapping("/illegal")
        void illegal() {
            throw new IllegalArgumentException("nope");
        }

        @GetMapping("/badcred")
        void badcred() {
            throw new BadCredentialsException("x");
        }

        @GetMapping("/denied")
        void denied() {
            throw new AccessDeniedException("x");
        }

        @GetMapping("/conflict-null")
        void conflictNull() {
            throw new ConflictFieldException(null, "msg");
        }

        record ValidDto(@Size(min = 3, message = "too short") String value) {}

        record TwoFields(@NotBlank(message = "a required") String a, @NotBlank(message = "b required") String b) {}
    }
}
