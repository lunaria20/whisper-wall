package com.sysintegg7.potane.whisperwall.shared.repository;

import com.sysintegg7.potane.whisperwall.shared.model.Role;
import com.sysintegg7.potane.whisperwall.shared.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsernameIgnoreCase(String username);
    Boolean existsByEmailIgnoreCase(String email);
    Long countByIsRestrictedTrue();
    Long countByRolesContains(Role role);
}
