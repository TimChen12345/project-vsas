package com.vsas.config;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Older deployments stored {@code GUEST} in {@code users.role}. The app now only uses USER / ADMIN
 * for registered accounts; this updates the database before any JPA {@code findAll()} runs.
 */
@Configuration
public class LegacyRoleSqlFixRunner {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    CommandLineRunner normalizeLegacyGuestRoles(DataSource dataSource) {
        return args -> {
            try (Connection c = dataSource.getConnection();
                    Statement st = c.createStatement()) {
                st.executeUpdate("UPDATE users SET role = 'USER' WHERE role = 'GUEST'");
            } catch (Exception ignored) {
                // e.g. table not created yet on a broken partial migration — other runners may fail loudly
            }
        };
    }
}
