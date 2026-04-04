package com.vsas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vsas.dto.AuthResponse;
import com.vsas.dto.ChangePasswordRequest;
import com.vsas.dto.LoginRequest;
import com.vsas.dto.RegisterRequest;
import com.vsas.dto.UpdateProfileRequest;
import com.vsas.dto.UserProfileResponse;
import com.vsas.entity.Role;
import com.vsas.entity.User;
import com.vsas.exception.ConflictFieldException;
import com.vsas.repository.ScrollRepository;
import com.vsas.repository.UserRepository;
import com.vsas.service.impl.UserServiceImpl;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ScrollRepository scrollRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;

    private JwtService jwtService;
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private User persistedUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);

        userService =
                new UserServiceImpl(
                        userRepository,
                        scrollRepository,
                        passwordEncoder,
                        jwtService,
                        authenticationManager,
                        userDetailsService);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("alice");
        registerRequest.setPassword("secret12");
        registerRequest.setIdKey("alice-key");
        registerRequest.setFullName("Alice");
        registerRequest.setEmail("a@b.com");
        registerRequest.setPhoneNumber("+10000000000");

        persistedUser = new User();
        persistedUser.setId(5L);
        persistedUser.setUsername("alice");
        persistedUser.setPassword("ENC");
        persistedUser.setIdKey("alice-key");
        persistedUser.setFullName("Alice");
        persistedUser.setEmail("a@b.com");
        persistedUser.setPhoneNumber("+10000000000");
        persistedUser.setRole(Role.USER);
        persistedUser.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    void register_savesUser_whenUnique() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByIdKey("alice-key")).thenReturn(false);
        when(passwordEncoder.encode("secret12")).thenReturn("ENC");

        userService.register(registerRequest);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throws_whenUsernameTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(ConflictFieldException.class)
                .hasFieldOrPropertyWithValue("field", "username");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throws_whenIdKeyTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByIdKey("alice-key")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest)).isInstanceOf(ConflictFieldException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsToken() {
        LoginRequest login = new LoginRequest();
        login.setUsername("alice");
        login.setPassword("secret12");
        UserDetails details =
                org.springframework.security.core.userdetails.User.withUsername("alice")
                        .password("ENC")
                        .roles("USER")
                        .build();
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(details);

        AuthResponse res = userService.login(login);

        assertThat(res.getToken()).isNotBlank();
        assertThat(res.getUsername()).isEqualTo("alice");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void getProfile_returnsDto() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(scrollRepository.countByUploader_Username("alice")).thenReturn(2L);

        UserProfileResponse p = userService.getProfile("alice");

        assertThat(p.getUsername()).isEqualTo("alice");
        assertThat(p.getScrollCount()).isEqualTo(2L);
        assertThat(p.getRole()).isEqualTo("USER");
    }

    @Test
    void getProfile_throws_whenMissing() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("nobody")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateProfile_updatesFields() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(scrollRepository.countByUploader_Username("alice")).thenReturn(0L);
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFullName("Alice L.");
        req.setEmail("new@b.com");

        UserProfileResponse p = userService.updateProfile("alice", req);

        assertThat(p.getFullName()).isEqualTo("Alice L.");
        assertThat(p.getEmail()).isEqualTo("new@b.com");
        verify(userRepository).save(persistedUser);
    }

    @Test
    void updateProfile_throws_onInvalidEmail() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("bad");

        assertThatThrownBy(() -> userService.updateProfile("alice", req))
                .isInstanceOf(ConflictFieldException.class)
                .hasFieldOrPropertyWithValue("field", "email");
    }

    @Test
    void updateProfile_clearsEmail_whenBlank() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(scrollRepository.countByUploader_Username("alice")).thenReturn(0L);
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("   ");

        userService.updateProfile("alice", req);

        assertThat(persistedUser.getEmail()).isNull();
    }

    @Test
    void changePassword_updates_whenCurrentMatches() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(passwordEncoder.matches("old", "ENC")).thenReturn(true);
        when(passwordEncoder.encode("newpass12")).thenReturn("NEWENC");
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("newpass12");

        userService.changePassword("alice", req);

        assertThat(persistedUser.getPassword()).isEqualTo("NEWENC");
        verify(userRepository).save(persistedUser);
    }

    @Test
    void changePassword_throws_whenCurrentWrong() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(passwordEncoder.matches("wrong", "ENC")).thenReturn(false);
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("wrong");
        req.setNewPassword("newpass12");

        assertThatThrownBy(() -> userService.changePassword("alice", req))
                .isInstanceOf(ConflictFieldException.class)
                .hasFieldOrPropertyWithValue("field", "currentPassword");
    }

    @Test
    void toProfile_mapsGuestToUser() {
        persistedUser.setRole(Role.GUEST);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(scrollRepository.countByUploader_Username("alice")).thenReturn(0L);

        UserProfileResponse p = userService.getProfile("alice");

        assertThat(p.getRole()).isEqualTo("USER");
    }

    @Test
    void getProfile_roleNull_defaultsToUser() {
        persistedUser.setRole(null);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(scrollRepository.countByUploader_Username("alice")).thenReturn(0L);

        assertThat(userService.getProfile("alice").getRole()).isEqualTo("USER");
    }

    @Test
    void updateProfile_skipsFullNameWhenNull() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(scrollRepository.countByUploader_Username("alice")).thenReturn(0L);
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("z@z.com");

        userService.updateProfile("alice", req);

        assertThat(persistedUser.getFullName()).isEqualTo("Alice");
    }

    @Test
    void updateProfile_idKeyConflict_whenTakenByAnother() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(userRepository.existsByIdKey("other-key")).thenReturn(true);
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setIdKey("other-key");

        assertThatThrownBy(() -> userService.updateProfile("alice", req))
                .isInstanceOf(ConflictFieldException.class)
                .hasFieldOrPropertyWithValue("field", "idKey");
    }

    @Test
    void updateProfile_idKeySameAsCurrent_noConflict() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(scrollRepository.countByUploader_Username("alice")).thenReturn(0L);
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setIdKey("alice-key");

        userService.updateProfile("alice", req);

        verify(userRepository, never()).existsByIdKey(any());
    }

    @Test
    void updateProfile_idKeyBlankAfterTrim_skips() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(scrollRepository.countByUploader_Username("alice")).thenReturn(0L);
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setIdKey("   ");

        userService.updateProfile("alice", req);

        assertThat(persistedUser.getIdKey()).isEqualTo("alice-key");
    }

    @Test
    void updateProfile_phoneBlankClears() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        when(scrollRepository.countByUploader_Username("alice")).thenReturn(0L);
        persistedUser.setPhoneNumber("+1");
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setPhoneNumber("  ");

        userService.updateProfile("alice", req);

        assertThat(persistedUser.getPhoneNumber()).isNull();
    }

    @Test
    void updateProfile_invalidEmail_domainMissing() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(persistedUser));
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("a@");

        assertThatThrownBy(() -> userService.updateProfile("alice", req))
                .isInstanceOf(ConflictFieldException.class)
                .hasFieldOrPropertyWithValue("field", "email");
    }

    @Test
    void changePassword_userNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("a");
        req.setNewPassword("bbbbbb");

        assertThatThrownBy(() -> userService.changePassword("ghost", req))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
