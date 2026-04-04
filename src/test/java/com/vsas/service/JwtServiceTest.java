package com.vsas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);
    }

    @Test
    void generateAndValidate_roundTrip() {
        UserDetails user =
                User.withUsername("alice").password("x").roles("USER").build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_falseForWrongUser() {
        UserDetails user =
                User.withUsername("alice").password("x").roles("USER").build();
        UserDetails other =
                User.withUsername("bob").password("x").roles("USER").build();
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void expiredToken_rejectedOnParse() throws Exception {
        JwtService shortLived = new JwtService();
        ReflectionTestUtils.setField(
                shortLived,
                "secret",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(shortLived, "expirationMs", 1L);
        UserDetails user =
                User.withUsername("exp").password("x").roles("USER").build();
        String token = shortLived.generateToken(user);

        Thread.sleep(25);

        assertThatThrownBy(() -> shortLived.extractUsername(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }
}
