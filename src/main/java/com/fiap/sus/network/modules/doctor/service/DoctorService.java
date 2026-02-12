package com.fiap.sus.network.modules.doctor.service;

import com.fiap.sus.network.modules.doctor.repository.DoctorRepository;

import com.fiap.sus.network.modules.doctor.dto.DoctorRequest;
import com.fiap.sus.network.modules.doctor.dto.DoctorResponse;
import com.fiap.sus.network.modules.doctor.entity.Doctor;
import com.fiap.sus.network.modules.doctor.mapper.DoctorMapper;
import com.fiap.sus.network.modules.specialty.repository.SpecialtyRepository;
import com.fiap.sus.network.modules.doctor.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.fiap.sus.network.modules.health_unit.repository.HealthUnitRepository;
import com.fiap.sus.network.modules.user.service.AccessControlService;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorMapper doctorMapper;
    private final HealthUnitRepository healthUnitRepository;
    private final AccessControlService accessControlService;

    @Transactional(readOnly = true)
    public Page<DoctorResponse> listByUnitId(UUID unitId, Pageable pageable) {
        log.info("Listing doctors. Unit filter: {}", unitId);
        if (unitId != null) {
            if (!healthUnitRepository.existsById(unitId)) {
                log.error("Health unit not found: {}", unitId);
                throw new ResourceNotFoundException("Health unit not found");
            }
            accessControlService.checkAccess(unitId);
            return doctorRepository.findAllByUnitId(unitId, pageable).map(doctorMapper::toDto);
        }
        return doctorRepository.findAll(pageable).map(doctorMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> listDoctors() {
        log.info("Listing all doctors");
        return doctorRepository.findAll().stream().map(doctorMapper::toDto).toList();
    }

    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        log.info("Creating doctor: {}", request.name());
        Doctor doctor = new Doctor();
        doctor.setName(request.name());
        doctor.setCrm(request.crm());
        
        if (request.specialtyIds() != null) {
            List<Specialty> specs = specialtyRepository.findAllById(request.specialtyIds());
            doctor.setSpecialties(new HashSet<>(specs));
        }
        
        return doctorMapper.toDto(doctorRepository.save(doctor));
    }

    @Transactional(readOnly = true)
    public DoctorResponse findById(UUID id) {
        log.info("Finding doctor by id: {}", id);
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting doctor: {}", id);
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        doctor.setDeleted(true);
        doctorRepository.save(doctor);
    }
}
