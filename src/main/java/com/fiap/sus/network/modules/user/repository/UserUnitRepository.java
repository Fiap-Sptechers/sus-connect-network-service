package com.fiap.sus.network.modules.user.repository;

import com.fiap.sus.network.modules.user.entity.UserUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserUnitRepository extends JpaRepository<UserUnit, UUID> {
    
    java.util.Optional<UserUnit> findByUserIdAndUnitId(UUID userId, UUID unitId);
    
    boolean existsByUserIdAndUnitId(UUID userId, UUID unitId);
    
    java.util.List<UserUnit> findByUnitId(UUID unitId);
}
