package com.fiap.sus.network.modules.doctor.repository;

import com.fiap.sus.network.modules.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    Optional<Doctor> findByCrm(String crm);

    @Query("SELECT DISTINCT d FROM Shift s JOIN s.doctors d WHERE s.unitId = :unitId")
    Page<Doctor> findAllByUnitId(UUID unitId, Pageable pageable);
}
