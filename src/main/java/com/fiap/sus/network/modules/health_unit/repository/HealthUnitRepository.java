package com.fiap.sus.network.modules.health_unit.repository;

import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;
import com.fiap.sus.network.modules.user.entity.UserUnit;

@Repository
public interface HealthUnitRepository extends JpaRepository<HealthUnit, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<HealthUnit> {
    
    @Query("SELECT u FROM HealthUnit u JOIN UserUnit uu ON u.id = uu.unit.id WHERE uu.user.id = :userId AND u.deleted = false AND uu.deleted = false")
    List<HealthUnit> findAllLinkedToUser(UUID userId);

    boolean existsByCnpj(String cnpj);
}