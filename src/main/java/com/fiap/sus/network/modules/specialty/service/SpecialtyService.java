package com.fiap.sus.network.modules.specialty.service;

import com.fiap.sus.network.modules.specialty.dto.SpecialtyRequest;
import com.fiap.sus.network.modules.specialty.dto.SpecialtyResponse;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import com.fiap.sus.network.modules.specialty.mapper.SpecialtyMapper;
import com.fiap.sus.network.modules.specialty.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    @Transactional(readOnly = true)
    public List<SpecialtyResponse> listSpecialties() {
        log.info("Listing all specialties");
        return specialtyRepository.findAll().stream().map(specialtyMapper::toDto).toList();
    }

    @Transactional
    public SpecialtyResponse createSpecialty(SpecialtyRequest request) {
        log.info("Creating specialty: {}", request.name());
        Specialty esp = new Specialty();
        esp.setName(request.name());
        return specialtyMapper.toDto(specialtyRepository.save(esp));
    }

    @Transactional(readOnly = true)
    public SpecialtyResponse findById(UUID id) {
        log.info("Finding specialty by id: {}", id);
        Specialty spec = specialtyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
        return specialtyMapper.toDto(spec);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting specialty: {}", id);
        Specialty spec = specialtyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
        spec.setDeleted(true);
        specialtyRepository.save(spec);
    }
}
