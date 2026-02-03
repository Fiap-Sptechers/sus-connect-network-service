package com.fiap.sus.network.modules.doctor.repository;

import com.fiap.sus.network.modules.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    java.util.Optional<Doctor> findByCrm(String crm);
}
