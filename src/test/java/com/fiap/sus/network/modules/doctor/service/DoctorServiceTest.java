package com.fiap.sus.network.modules.doctor.service;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.modules.doctor.dto.DoctorRequest;
import com.fiap.sus.network.modules.doctor.dto.DoctorResponse;
import com.fiap.sus.network.modules.doctor.entity.Doctor;
import com.fiap.sus.network.modules.doctor.mapper.DoctorMapper;
import com.fiap.sus.network.modules.doctor.repository.DoctorRepository;
import com.fiap.sus.network.modules.specialty.repository.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private SpecialtyRepository specialtyRepository;
    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService service;

    @Test
    void listDoctors_ShouldReturnList() {
        when(doctorRepository.findAll()).thenReturn(List.of(new Doctor()));
        when(doctorMapper.toDto(any())).thenReturn(new DoctorResponse(UUID.randomUUID(), "Dr. Test", "CRM123", Set.of()));

        List<DoctorResponse> result = service.listDoctors();

        assertFalse(result.isEmpty());
        verify(doctorRepository).findAll();
    }

    @Test
    void createDoctor_ShouldSaveDoctor() {
        DoctorRequest request = new DoctorRequest("Dr. Test", "CRM123", Set.of());
        Doctor doctor = new Doctor();
        doctor.setName(request.name());
        doctor.setCrm(request.crm());

        when(doctorRepository.save(any())).thenReturn(doctor);
        when(doctorMapper.toDto(doctor)).thenReturn(new DoctorResponse(UUID.randomUUID(), "Dr. Test", "CRM123", Set.of()));

        DoctorResponse response = service.createDoctor(request);

        assertNotNull(response);
        verify(doctorRepository).save(any());
    }

    @Test
    void findById_ShouldReturnDoctor_WhenFound() {
        UUID id = UUID.randomUUID();
        Doctor doctor = new Doctor();
        when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toDto(doctor)).thenReturn(new DoctorResponse(id, "Dr. Test", "CRM123", Set.of()));

        DoctorResponse response = service.findById(id);

        assertNotNull(response);
        assertEquals(id, response.id());
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(doctorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void delete_ShouldMarkAsDeleted_WhenFound() {
        UUID id = UUID.randomUUID();
        Doctor doctor = new Doctor();
        doctor.setDeleted(false);

        when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));

        service.delete(id);

        assertTrue(doctor.isDeleted());
        verify(doctorRepository).save(doctor);
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(doctorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
    }
}
