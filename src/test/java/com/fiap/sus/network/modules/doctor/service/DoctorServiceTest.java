package com.fiap.sus.network.modules.doctor.service;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.modules.doctor.dto.DoctorRequest;
import com.fiap.sus.network.modules.doctor.dto.DoctorResponse;
import com.fiap.sus.network.modules.doctor.entity.Doctor;
import com.fiap.sus.network.modules.doctor.mapper.DoctorMapper;
import com.fiap.sus.network.modules.doctor.repository.DoctorRepository;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import com.fiap.sus.network.modules.specialty.repository.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository repository;
    @Mock
    private SpecialtyRepository specialtyRepository;
    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService service;

    @Test
    void createDoctor_ShouldSave_WhenSpecialtiesExist() {
        UUID specId = UUID.randomUUID();
        DoctorRequest request = new DoctorRequest("Dr. Test", "123456-SP", new HashSet<>(Collections.singletonList(specId)));
        
        when(specialtyRepository.findAllById(request.specialtyIds())).thenReturn(List.of(new Specialty()));
        
        Doctor savedDoctor = new Doctor();
        savedDoctor.setId(UUID.randomUUID());
        savedDoctor.setName("Dr. Test");
        
        when(repository.save(any(Doctor.class))).thenReturn(savedDoctor);
        when(doctorMapper.toDto(savedDoctor)).thenReturn(new DoctorResponse(savedDoctor.getId(), "Dr. Test", "123456-SP", Collections.emptySet()));

        DoctorResponse response = service.createDoctor(request);

        assertNotNull(response);
        assertEquals("Dr. Test", response.name());
        verify(repository).save(any(Doctor.class));
    }

    @Test
    void delete_ShouldMarkAsDeleted() {
        UUID docId = UUID.randomUUID();
        Doctor doctor = new Doctor();
        doctor.setDeleted(false);
        
        when(repository.findById(docId)).thenReturn(Optional.of(doctor));
        
        service.delete(docId);
        
        assertTrue(doctor.isDeleted());
        verify(repository).save(doctor);
    }
    
    @Test
    void listDoctors_ShouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(new Doctor()));
        when(doctorMapper.toDto(any())).thenReturn(new DoctorResponse(UUID.randomUUID(), "Dr", "CRM", Collections.emptySet()));
        
        List<DoctorResponse> result = service.listDoctors();
        
        assertFalse(result.isEmpty());
    }
}
