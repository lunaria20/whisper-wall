package com.sysintegg7.potane.whisperwall.shared.repository;

import com.sysintegg7.potane.whisperwall.shared.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
