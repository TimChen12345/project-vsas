package com.vsas.config;

import com.vsas.entity.User;
import com.vsas.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class IdKeyBackfillRunner {

    @Bean
    @Order(2)
    CommandLineRunner backfillMissingIdKeys(UserRepository userRepository) {
        return args -> {
            for (User u : userRepository.findAll()) {
                if (u.getIdKey() == null || u.getIdKey().isBlank()) {
                    String k = "whisker-" + u.getId();
                    String candidate = k;
                    int i = 0;
                    while (userRepository.existsByIdKey(candidate)) {
                        i++;
                        candidate = k + "-" + i;
                    }
                    u.setIdKey(candidate);
                    if (u.getFullName() == null || u.getFullName().isBlank()) {
                        u.setFullName(u.getUsername());
                    }
                    userRepository.save(u);
                }
            }
        };
    }
}
