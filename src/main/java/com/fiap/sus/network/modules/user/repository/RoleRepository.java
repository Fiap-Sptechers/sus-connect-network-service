package com.fiap.sus.network.modules.user.repository;

import com.fiap.sus.network.modules.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    java.util.Optional<Role> findByName(String name);
}
