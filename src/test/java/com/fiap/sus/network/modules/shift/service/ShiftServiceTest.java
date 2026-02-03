package com.fiap.sus.network.modules.shift.service;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.modules.doctor.entity.Doctor;
import com.fiap.sus.network.modules.doctor.repository.DoctorRepository;
import com.fiap.sus.network.modules.shift.dto.ShiftScheduleRequest;
import com.fiap.sus.network.modules.shift.dto.ShiftUpdateRequest;
import com.fiap.sus.network.modules.shift.entity.Shift;
import com.fiap.sus.network.modules.shift.repository.ShiftRepository;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import com.fiap.sus.network.modules.specialty.enums.SpecialtyEnum;
import com.fiap.sus.network.modules.specialty.repository.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftRepository repository;
    @Mock
    private SpecialtyRepository specialtyRepository;
    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private ShiftService service;

    @Test
    void updateShift_ShouldCreateShift_WhenNotFound() {
        UUID unitId = UUID.randomUUID();
        ShiftUpdateRequest request = new ShiftUpdateRequest(unitId, SpecialtyEnum.CLINICA_GERAL, 10);
        
        when(repository.findByUnitIdAndSpecialtyName(unitId, "CLINICA_GERAL"))
            .thenReturn(Optional.empty());
            
        when(specialtyRepository.findByName("CLINICA_GERAL"))
            .thenReturn(Optional.of(new Specialty()));

        service.updateShift(request);

        verify(repository).save(any(Shift.class));
    }

    @Test
    void updateShift_ShouldUpdate_WhenFound() {
        UUID unitId = UUID.randomUUID();
        ShiftUpdateRequest request = new ShiftUpdateRequest(unitId, SpecialtyEnum.CLINICA_GERAL, 10);
        
        Shift existingShift = new Shift();
        existingShift.setWaitingPatients(5);
        
        when(repository.findByUnitIdAndSpecialtyName(unitId, "CLINICA_GERAL"))
            .thenReturn(Optional.of(existingShift));

        service.updateShift(request);

        assertEquals(10, existingShift.getWaitingPatients());
        verify(repository).save(existingShift);
    }
    
    @Test
    void defineSchedule_ShouldUpdateDoctors() {
        UUID unitId = UUID.randomUUID();
        List<String> crms = List.of("123");
        ShiftScheduleRequest request = new ShiftScheduleRequest(unitId, SpecialtyEnum.CLINICA_GERAL, crms);
        
        Shift existingShift = new Shift();
        existingShift.setDoctors(new HashSet<>());
        
        when(repository.findByUnitIdAndSpecialtyName(unitId, "CLINICA_GERAL"))
            .thenReturn(Optional.of(existingShift));
            
        when(doctorRepository.findByCrm("123")).thenReturn(Optional.of(new Doctor()));

        service.defineSchedule(request);

        assertEquals(1, existingShift.getDoctors().size());
        verify(repository).save(existingShift);
    }
    
    @Test
    void defineSchedule_ShouldThrow_WhenDoctorNotFound() {
        UUID unitId = UUID.randomUUID();
        List<String> crms = List.of("123");
        ShiftScheduleRequest request = new ShiftScheduleRequest(unitId, SpecialtyEnum.CLINICA_GERAL, crms);
        
        when(repository.findByUnitIdAndSpecialtyName(any(), any())).thenReturn(Optional.of(new Shift()));
        when(doctorRepository.findByCrm("123")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> service.defineSchedule(request));
    }
}
