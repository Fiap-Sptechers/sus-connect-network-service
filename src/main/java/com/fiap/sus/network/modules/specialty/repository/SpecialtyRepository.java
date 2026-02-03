package com.fiap.sus.network.modules.specialty.repository;

import com.fiap.sus.network.modules.specialty.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {
    
    java.util.Optional<Specialty> findByName(String name);
}
