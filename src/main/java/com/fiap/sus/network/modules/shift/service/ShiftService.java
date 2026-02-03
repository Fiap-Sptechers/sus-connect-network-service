package com.fiap.sus.network.modules.shift.service;

import com.fiap.sus.network.modules.shift.dto.ShiftUpdate;
import com.fiap.sus.network.modules.shift.entity.Shift;
import com.fiap.sus.network.modules.shift.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiap.sus.network.modules.shift.dto.ShiftUpdateRequest;
import com.fiap.sus.network.modules.shift.dto.ShiftScheduleRequest;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import com.fiap.sus.network.modules.specialty.repository.SpecialtyRepository;
import com.fiap.sus.network.modules.doctor.repository.DoctorRepository;
import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import java.util.HashSet;
import java.util.Set;
import com.fiap.sus.network.modules.doctor.entity.Doctor;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftService {

    private final ShiftRepository repository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public void updateShift(ShiftUpdateRequest dto) {
        log.info("Updating shift for unit {} specialty {}", dto.unitId(), dto.specialty());
        
        String specName = dto.specialty().name();
        
        Shift shift = repository.findByUnitIdAndSpecialtyName(dto.unitId(), specName)
                .orElseGet(() -> {
                     Specialty esp = specialtyRepository.findByName(specName)
                             .orElseGet(() -> specialtyRepository.save(createSpecialty(specName)));
                     return new Shift(null, dto.unitId(), esp, 0, new HashSet<>());
                });

        shift.setWaitingPatients(dto.waitingPatients());
        
        repository.save(shift);
    }

    @Transactional
    public void defineSchedule(ShiftScheduleRequest dto) {
        log.info("Defining schedule for unit {} specialty {}", dto.unitId(), dto.specialty());
        
        String specName = dto.specialty().name();
        
        Shift shift = repository.findByUnitIdAndSpecialtyName(dto.unitId(), specName)
                .orElseGet(() -> {
                     Specialty esp = specialtyRepository.findByName(specName)
                             .orElseGet(() -> specialtyRepository.save(createSpecialty(specName)));
                     return new Shift(null, dto.unitId(), esp, 0, new HashSet<>());
                });

        Set<Doctor> doctors = dto.doctorCrms().stream()
                .map(crm -> doctorRepository.findByCrm(crm)
                        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with CRM: " + crm)))
                .collect(Collectors.toSet());

        shift.setDoctors(doctors);
        
        repository.save(shift);
    }

    private Specialty createSpecialty(String name) {
        Specialty s = new Specialty();
        s.setName(name);
        return s;
    }
}
