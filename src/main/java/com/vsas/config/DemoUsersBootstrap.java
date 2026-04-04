package com.vsas.config;

import com.vsas.entity.Role;
import com.vsas.entity.User;
import com.vsas.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUsersBootstrap {

    /** Single built-in admin account (assignment). Password: admin123 */
    @Bean
    @Order(1)
    CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.existsByUsername("admin")) {
                return;
            }
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setIdKey("SYS-ADMIN");
            admin.setFullName("System Administrator");
            admin.setEmail("admin@vsas.local");
            admin.setPhoneNumber("+10000000001");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        };
    }
}
