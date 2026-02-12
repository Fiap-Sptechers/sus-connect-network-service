package com.fiap.sus.network.modules.health_unit.repository;

import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthUnitRepository extends JpaRepository<HealthUnit, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<HealthUnit> {

    @EntityGraph(attributePaths = {"contacts", "address"})
    Optional<HealthUnit> findById(UUID id);

    @Query("SELECT u FROM HealthUnit u JOIN UserUnit uu ON u.id = uu.unit.id WHERE uu.user.id = :userId AND u.deleted = false AND uu.deleted = false")
    List<HealthUnit> findAllLinkedToUser(UUID userId);

    boolean existsByCnpj(String cnpj);
}