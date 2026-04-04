package com.vsas.service.impl;

import com.vsas.dto.AdminCreateUserRequest;
import com.vsas.dto.AdminUserResponse;
import com.vsas.dto.ScrollStatsRowResponse;
import com.vsas.entity.Role;
import com.vsas.entity.Scroll;
import com.vsas.entity.User;
import com.vsas.exception.ConflictFieldException;
import com.vsas.repository.ScrollRepository;
import com.vsas.repository.UserRepository;
import com.vsas.service.AdminService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ScrollRepository scrollRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(
            UserRepository userRepository, ScrollRepository scrollRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.scrollRepository = scrollRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toAdminRow).toList();
    }

    @Override
    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictFieldException(
                    "username",
                    "This username is already taken. Choose another (3–64 characters).");
        }
        if (userRepository.existsByIdKey(request.getIdKey().trim())) {
            throw new ConflictFieldException(
                    "idKey",
                    "This ID key is already taken. Each user needs a unique ID key.");
        }
        User u = new User();
        u.setUsername(request.getUsername().trim());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setIdKey(request.getIdKey().trim());
        u.setFullName(request.getFullName().trim());
        u.setEmail(request.getEmail().trim());
        u.setPhoneNumber(request.getPhoneNumber().trim());
        u.setRole(request.getRole());
        userRepository.save(u);
        return toAdminRow(u);
    }

    @Override
    @Transactional
    public void deleteUser(Long targetUserId, String actingAdminUsername) {
        User admin = userRepository
                .findByUsername(actingAdminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        if (targetUserId.equals(admin.getId())) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }
        User target = userRepository
                .findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(target);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrollStatsRowResponse> scrollStats() {
        return scrollRepository.findAll().stream().map(this::toStatsRow).toList();
    }

    private AdminUserResponse toAdminRow(User u) {
        Role r = u.getRole() != null ? u.getRole() : Role.USER;
        if (r == Role.GUEST) {
            r = Role.USER;
        }
        return new AdminUserResponse(
                u.getId(),
                u.getUsername(),
                u.getIdKey(),
                u.getFullName(),
                u.getEmail(),
                u.getPhoneNumber(),
                r.name(),
                u.getCreatedAt());
    }

    private ScrollStatsRowResponse toStatsRow(Scroll s) {
        User u = s.getUploader();
        ScrollStatsRowResponse r = new ScrollStatsRowResponse();
        r.setScrollDbId(s.getId());
        r.setScrollId(s.getScrollId());
        r.setName(s.getName());
        r.setUploaderIdKey(u.getIdKey());
        r.setUploaderUsername(u.getUsername());
        r.setUploadedAt(s.getUploadedAt());
        r.setDownloadCount(s.getDownloadCount());
        r.setUploadEventCount(s.getUploadEventCount());
        r.setSizeBytes(s.getData() != null ? s.getData().length : 0);
        return r;
    }
}
