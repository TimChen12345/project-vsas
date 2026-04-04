package com.vsas.repository;

import com.vsas.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByIdKey(String idKey);

    Optional<User> findByIdKey(String idKey);
}
