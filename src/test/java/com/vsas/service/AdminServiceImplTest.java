package com.vsas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vsas.dto.AdminCreateUserRequest;
import com.vsas.dto.AdminUserResponse;
import com.vsas.dto.ScrollStatsRowResponse;
import com.vsas.entity.Role;
import com.vsas.entity.Scroll;
import com.vsas.entity.User;
import com.vsas.exception.ConflictFieldException;
import com.vsas.repository.ScrollRepository;
import com.vsas.repository.UserRepository;
import com.vsas.service.impl.AdminServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ScrollRepository scrollRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AdminServiceImpl adminService;

    @Test
    void listUsers_mapsRows() {
        User u = new User();
        u.setId(1L);
        u.setUsername("u1");
        u.setIdKey("k1");
        u.setFullName("F");
        u.setEmail("e@e.com");
        u.setPhoneNumber("+1");
        u.setRole(Role.USER);
        u.setCreatedAt(Instant.now());
        when(userRepository.findAll()).thenReturn(List.of(u));

        List<AdminUserResponse> rows = adminService.listUsers();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getUsername()).isEqualTo("u1");
    }

    @Test
    void createUser_saves() {
        AdminCreateUserRequest req = new AdminCreateUserRequest();
        req.setUsername("nu");
        req.setPassword("longpass");
        req.setIdKey("nk");
        req.setFullName("N");
        req.setEmail("n@n.com");
        req.setPhoneNumber("+1999");
        req.setRole(Role.USER);
        when(userRepository.existsByUsername("nu")).thenReturn(false);
        when(userRepository.existsByIdKey("nk")).thenReturn(false);
        when(passwordEncoder.encode("longpass")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUserResponse row = adminService.createUser(req);

        assertThat(row.getUsername()).isEqualTo("nu");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_conflictUsername() {
        AdminCreateUserRequest req = new AdminCreateUserRequest();
        req.setUsername("nu");
        req.setPassword("longpass");
        req.setIdKey("nk");
        req.setFullName("N");
        req.setEmail("n@n.com");
        req.setPhoneNumber("+1999");
        req.setRole(Role.USER);
        when(userRepository.existsByUsername("nu")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser(req)).isInstanceOf(ConflictFieldException.class);
    }

    @Test
    void deleteUser_blocksSelf() {
        User admin = new User();
        admin.setId(99L);
        admin.setUsername("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminService.deleteUser(99L, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own account");
    }

    @Test
    void deleteUser_removesOther() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        User target = new User();
        target.setId(2L);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        adminService.deleteUser(2L, "admin");

        verify(userRepository).delete(target);
    }

    @Test
    void scrollStats_mapsScrolls() {
        User up = new User();
        up.setIdKey("uk");
        up.setUsername("uu");
        Scroll s = new Scroll();
        s.setId(5L);
        s.setScrollId("z");
        s.setName("n");
        s.setUploader(up);
        s.setUploadedAt(Instant.now());
        s.setDownloadCount(3);
        s.setUploadEventCount(1);
        s.setData(new byte[] {1, 2});
        when(scrollRepository.findAll()).thenReturn(List.of(s));

        List<ScrollStatsRowResponse> rows = adminService.scrollStats();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getDownloadCount()).isEqualTo(3);
    }

    @Test
    void toAdminRow_mapsGuestToUser() {
        User u = new User();
        u.setId(1L);
        u.setUsername("g");
        u.setIdKey("gk");
        u.setFullName("G");
        u.setEmail("g@g.com");
        u.setPhoneNumber("+1");
        u.setRole(Role.GUEST);
        u.setCreatedAt(Instant.now());
        when(userRepository.findAll()).thenReturn(List.of(u));

        List<AdminUserResponse> rows = adminService.listUsers();

        assertThat(rows.get(0).getRole()).isEqualTo("USER");
    }

    @Test
    void createUser_throwsWhenIdKeyTaken() {
        AdminCreateUserRequest req = new AdminCreateUserRequest();
        req.setUsername("nu2");
        req.setPassword("longpass");
        req.setIdKey("taken");
        req.setFullName("N");
        req.setEmail("n@n.com");
        req.setPhoneNumber("+1999");
        req.setRole(Role.USER);
        when(userRepository.existsByUsername("nu2")).thenReturn(false);
        when(userRepository.existsByIdKey("taken")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser(req)).isInstanceOf(ConflictFieldException.class);
    }

    @Test
    void deleteUser_throwsWhenTargetMissing() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(999L, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_throwsWhenAdminMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(2L, "ghost"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Admin not found");
    }

    @Test
    void toAdminRow_roleNull_defaultsUser() {
        User u = new User();
        u.setId(1L);
        u.setUsername("r");
        u.setIdKey("rk");
        u.setFullName("R");
        u.setEmail("r@r.com");
        u.setPhoneNumber("+1");
        u.setRole(null);
        u.setCreatedAt(Instant.now());
        when(userRepository.findAll()).thenReturn(List.of(u));

        assertThat(adminService.listUsers().get(0).getRole()).isEqualTo("USER");
    }
}
