package com.fiap.sus.network.modules.shift.repository;

import com.fiap.sus.network.modules.shift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    Optional<Shift> findByUnitIdAndSpecialtyName(UUID unitId, String specialtyName);
    List<Shift> findByUnitId(UUID unitId);
    Page<Shift> findByUnitId(UUID unitId, Pageable pageable);
}
