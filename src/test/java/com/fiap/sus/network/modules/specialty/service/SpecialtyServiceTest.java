package com.fiap.sus.network.modules.specialty.service;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.modules.specialty.dto.SpecialtyRequest;
import com.fiap.sus.network.modules.specialty.dto.SpecialtyResponse;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import com.fiap.sus.network.modules.specialty.mapper.SpecialtyMapper;
import com.fiap.sus.network.modules.specialty.repository.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;
    @Mock
    private SpecialtyMapper specialtyMapper;

    @InjectMocks
    private SpecialtyService service;

    @Test
    void listSpecialties_ShouldReturnList() {
        when(specialtyRepository.findAll()).thenReturn(List.of(new Specialty()));
        when(specialtyMapper.toDto(any())).thenReturn(new SpecialtyResponse(UUID.randomUUID(), "Cardiology"));

        List<SpecialtyResponse> result = service.listSpecialties();

        assertFalse(result.isEmpty());
        verify(specialtyRepository).findAll();
    }

    @Test
    void createSpecialty_ShouldSaveSpecialty() {
        SpecialtyRequest request = new SpecialtyRequest("Cardiology");
        Specialty specialty = new Specialty();
        specialty.setName(request.name());

        when(specialtyRepository.save(any())).thenReturn(specialty);
        when(specialtyMapper.toDto(specialty)).thenReturn(new SpecialtyResponse(UUID.randomUUID(), "Cardiology"));

        SpecialtyResponse response = service.createSpecialty(request);

        assertNotNull(response);
        verify(specialtyRepository).save(any());
    }

    @Test
    void findById_ShouldReturnSpecialty_WhenFound() {
        UUID id = UUID.randomUUID();
        Specialty specialty = new Specialty();
        when(specialtyRepository.findById(id)).thenReturn(Optional.of(specialty));
        when(specialtyMapper.toDto(specialty)).thenReturn(new SpecialtyResponse(id, "Cardiology"));

        SpecialtyResponse response = service.findById(id);

        assertNotNull(response);
        assertEquals(id, response.id());
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(specialtyRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void delete_ShouldMarkAsDeleted_WhenFound() {
        UUID id = UUID.randomUUID();
        Specialty specialty = new Specialty();
        specialty.setDeleted(false);

        when(specialtyRepository.findById(id)).thenReturn(Optional.of(specialty));

        service.delete(id);

        assertTrue(specialty.isDeleted());
        verify(specialtyRepository).save(specialty);
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(specialtyRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
    }
}
