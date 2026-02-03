package com.fiap.sus.network.modules.user.repository;

import com.fiap.sus.network.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByCpfCnpj(String cpfCnpj);
}
