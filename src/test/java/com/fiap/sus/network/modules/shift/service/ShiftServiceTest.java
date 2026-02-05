package com.fiap.sus.network.modules.shift.service;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.modules.doctor.entity.Doctor;
import com.fiap.sus.network.modules.doctor.repository.DoctorRepository;
import com.fiap.sus.network.modules.specialty.enums.SpecialtyEnum;
import com.fiap.sus.network.modules.shift.dto.ShiftScheduleRequest;
import com.fiap.sus.network.modules.shift.dto.ShiftUpdateRequest;
import com.fiap.sus.network.modules.shift.entity.Shift;
import com.fiap.sus.network.modules.shift.repository.ShiftRepository;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import com.fiap.sus.network.modules.specialty.repository.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void updateShift_ShouldSaveShift() {
        UUID unitId = UUID.randomUUID();
        ShiftUpdateRequest request = new ShiftUpdateRequest(unitId, SpecialtyEnum.CLINICA_GERAL, 5);

        when(repository.findByUnitIdAndSpecialtyName(any(), any())).thenReturn(Optional.empty());
        when(specialtyRepository.findByName(any())).thenReturn(Optional.of(new Specialty()));

        service.updateShift(request);

        verify(repository).save(any());
    }

    @Test
    void defineSchedule_ShouldSaveShift() {
        UUID unitId = UUID.randomUUID();
        String crm = "CRM/SP 123456";
        ShiftScheduleRequest request = new ShiftScheduleRequest(unitId, SpecialtyEnum.CLINICA_GERAL, List.of(crm));

        when(repository.findByUnitIdAndSpecialtyName(any(), any())).thenReturn(Optional.of(new Shift()));
        when(doctorRepository.findByCrm(crm)).thenReturn(Optional.of(new Doctor()));

        service.defineSchedule(request);

        verify(repository).save(any());
    }

    @Test
    void defineSchedule_ShouldThrowException_WhenDoctorNotFound() {
        UUID unitId = UUID.randomUUID();
        String crm = "CRM/SP 123456";
        ShiftScheduleRequest request = new ShiftScheduleRequest(unitId, SpecialtyEnum.CLINICA_GERAL, List.of(crm));

        when(repository.findByUnitIdAndSpecialtyName(any(), any())).thenReturn(Optional.of(new Shift()));
        when(doctorRepository.findByCrm(crm)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.defineSchedule(request));
    }
}
