package com.vsas.service.impl;

import com.vsas.dto.AuthResponse;
import com.vsas.dto.ChangePasswordRequest;
import com.vsas.dto.LoginRequest;
import com.vsas.dto.RegisterRequest;
import com.vsas.dto.UpdateProfileRequest;
import com.vsas.dto.UserProfileResponse;
import com.vsas.entity.Role;
import com.vsas.entity.User;
import com.vsas.repository.ScrollRepository;
import com.vsas.repository.UserRepository;
import com.vsas.exception.ConflictFieldException;
import com.vsas.service.JwtService;
import com.vsas.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ScrollRepository scrollRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public UserServiceImpl(
            UserRepository userRepository,
            ScrollRepository scrollRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.scrollRepository = scrollRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictFieldException(
                    "username",
                    "This login username is already registered. Choose a different username (3–64 characters), "
                            + "or use Sign in if you already have an account.");
        }
        String idKey = request.getIdKey().trim();
        if (userRepository.existsByIdKey(idKey)) {
            throw new ConflictFieldException(
                    "idKey",
                    "This ID key is already used by another user. Pick a unique ID key (2–64 characters; "
                            + "letters, digits, . _ - only; must start with a letter or digit).");
        }
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIdKey(idKey);
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setRole(Role.USER);
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, request.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toProfile(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.getFullName() != null) {
            String f = request.getFullName().trim();
            user.setFullName(f.isEmpty() ? null : f);
        }
        if (request.getIdKey() != null) {
            String k = request.getIdKey().trim();
            if (!k.isEmpty()) {
                if (!k.equals(user.getIdKey()) && userRepository.existsByIdKey(k)) {
                    throw new ConflictFieldException(
                            "idKey",
                            "This ID key is already used by another user. Choose a different value.");
                }
                user.setIdKey(k);
            }
        }
        if (request.getEmail() != null) {
            String e = request.getEmail().trim();
            if (!e.isEmpty()) {
                validateEmail(e);
                user.setEmail(e);
            } else {
                user.setEmail(null);
            }
        }
        if (request.getPhoneNumber() != null) {
            String ph = request.getPhoneNumber().trim();
            user.setPhoneNumber(ph.isEmpty() ? null : ph);
        }
        userRepository.save(user);
        return toProfile(user);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ConflictFieldException(
                    "currentPassword",
                    "Current password does not match our records. Re-enter your existing password exactly.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserProfileResponse toProfile(User user) {
        long scrollCount = scrollRepository.countByUploader_Username(user.getUsername());
        Role role = user.getRole() != null ? user.getRole() : Role.USER;
        if (role == Role.GUEST) {
            role = Role.USER;
        }
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getIdKey(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                scrollCount,
                role.name());
    }

    private static void validateEmail(String email) {
        int at = email.indexOf('@');
        if (at < 1 || at == email.length() - 1) {
            throw new ConflictFieldException(
                    "email",
                    "Email must include text before @, the @ symbol, and a domain after it (e.g. name@example.com).");
        }
    }
}
