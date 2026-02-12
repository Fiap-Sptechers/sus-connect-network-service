package com.fiap.sus.network.modules.shift.service;

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
import com.fiap.sus.network.modules.shift.mapper.ShiftMapper;
import com.fiap.sus.network.modules.shift.dto.ShiftResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;
import java.util.UUID;

import com.fiap.sus.network.modules.health_unit.repository.HealthUnitRepository;
import com.fiap.sus.network.modules.user.service.AccessControlService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftService {

    private final ShiftRepository repository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final ShiftMapper shiftMapper;
    private final HealthUnitRepository healthUnitRepository;
    private final AccessControlService accessControlService;

    @Transactional(readOnly = true)
    public Page<ShiftResponse> listShifts(UUID unitId, Pageable pageable) {
        log.info("Listing shifts for unit: {}", unitId);
        validateUnitAndAccess(unitId);
        return repository.findByUnitId(unitId, pageable)
                .map(shiftMapper::toDto);
    }

    @Transactional
    public void updateShift(ShiftUpdateRequest dto) {
        log.info("Updating shift capacity for unit: {}, specialty: {}", dto.unitId(), dto.specialty());
        validateUnitAndAccess(dto.unitId());
        
        String specName = dto.specialty().name();
        
        Shift shift = repository.findByUnitIdAndSpecialtyName(dto.unitId(), specName)
                .orElseGet(() -> {
                     log.info("Creating new shift for unit: {} and specialty: {}", dto.unitId(), specName);
                     Specialty esp = specialtyRepository.findByName(specName)
                             .orElseGet(() -> {
                                 log.info("Specialty not found, creating: {}", specName);
                                 return specialtyRepository.save(createSpecialty(specName));
                             });
                     return new Shift(null, dto.unitId(), esp, 0, new HashSet<>());
                });

        shift.setCapacity(dto.capacity());
        repository.save(shift);
        log.info("Shift updated successfully: {}", shift.getId());
    }

    @Transactional
    public void defineSchedule(ShiftScheduleRequest dto) {
        log.info("Defining schedule for unit: {}, specialty: {}", dto.unitId(), dto.specialty());
        validateUnitAndAccess(dto.unitId());
        
        String specName = dto.specialty().name();
        
        Shift shift = repository.findByUnitIdAndSpecialtyName(dto.unitId(), specName)
                .orElseGet(() -> {
                     log.info("Creating new shift for unit: {} and specialty: {}", dto.unitId(), specName);
                     Specialty esp = specialtyRepository.findByName(specName)
                             .orElseGet(() -> {
                                 log.info("Specialty not found, creating: {}", specName);
                                 return specialtyRepository.save(createSpecialty(specName));
                             });
                     return new Shift(null, dto.unitId(), esp, 0, new HashSet<>());
                });

        Set<Doctor> doctors = dto.doctorCrms().stream()
                .map(crm -> doctorRepository.findByCrm(crm)
                        .orElseThrow(() -> {
                            log.error("Doctor not found with CRM: {}", crm);
                            return new ResourceNotFoundException("Doctor not found with CRM: " + crm);
                        }))
                .collect(Collectors.toSet());

        shift.setDoctors(doctors);
        repository.save(shift);
        log.info("Shift schedule defined successfully for unit: {} and specialty: {}", dto.unitId(), specName);
    }

    @Transactional
    public void deleteShift(UUID id) {
        log.info("Deleting shift: {}", id);
        Shift shift = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
        
        validateUnitAndAccess(shift.getUnitId());
        
        shift.setDeleted(true);
        repository.save(shift);
        log.info("Shift deleted successfully: {}", id);
    }

    private void validateUnitAndAccess(UUID unitId) {
        if (!healthUnitRepository.existsById(unitId)) {
            log.error("Health unit not found: {}", unitId);
            throw new ResourceNotFoundException("Health unit not found");
        }
        accessControlService.checkAccess(unitId);
    }

    private Specialty createSpecialty(String name) {
        Specialty s = new Specialty();
        s.setName(name);
        return s;
    }
}
