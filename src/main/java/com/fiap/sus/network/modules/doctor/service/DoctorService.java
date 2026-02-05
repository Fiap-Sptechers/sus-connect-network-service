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
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorMapper doctorMapper;

    public List<DoctorResponse> listDoctors() {
        return doctorRepository.findAll().stream().map(doctorMapper::toDto).toList();
    }

    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        Doctor doctor = new Doctor();
        doctor.setName(request.name());
        doctor.setCrm(request.crm());
        
        if (request.specialtyIds() != null) {
            List<Specialty> specs = specialtyRepository.findAllById(request.specialtyIds());
            doctor.setSpecialties(new HashSet<>(specs));
        }
        
        return doctorMapper.toDto(doctorRepository.save(doctor));
    }

    public DoctorResponse findById(UUID id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public void delete(UUID id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        doctor.setDeleted(true);
        doctorRepository.save(doctor);
    }
}
