package com.vsas.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.vsas.entity.Role;
import com.vsas.entity.User;
import com.vsas.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_returnsAuthorities() {
        User u = new User();
        u.setUsername("alice");
        u.setPassword("ENC");
        u.setRole(Role.ADMIN);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        UserDetails d = userDetailsService.loadUserByUsername("alice");

        assertThat(d.getUsername()).isEqualTo("alice");
        assertThat(d.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_mapsGuestToUserRole() {
        User u = new User();
        u.setUsername("g");
        u.setPassword("ENC");
        u.setRole(Role.GUEST);
        when(userRepository.findByUsername("g")).thenReturn(Optional.of(u));

        UserDetails d = userDetailsService.loadUserByUsername("g");

        assertThat(d.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void loadUserByUsername_throwsWhenMissing() {
        when(userRepository.findByUsername("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nope"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_roleNull_defaultsUser() {
        User u = new User();
        u.setUsername("norole");
        u.setPassword("ENC");
        u.setRole(null);
        when(userRepository.findByUsername("norole")).thenReturn(Optional.of(u));

        UserDetails d = userDetailsService.loadUserByUsername("norole");

        assertThat(d.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }
}
