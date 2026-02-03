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
    private SpecialtyRepository repository;
    @Mock
    private SpecialtyMapper mapper;

    @InjectMocks
    private SpecialtyService service;

    @Test
    void listSpecialties_ShouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(new Specialty()));
        when(mapper.toDto(any())).thenReturn(new SpecialtyResponse(UUID.randomUUID(), "Test"));
        
        List<SpecialtyResponse> result = service.listSpecialties();
        
        assertFalse(result.isEmpty());
    }

    @Test
    void createSpecialty_ShouldSave() {
        SpecialtyRequest request = new SpecialtyRequest("Test");
        Specialty saved = new Specialty();
        
        when(repository.save(any(Specialty.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(new SpecialtyResponse(UUID.randomUUID(), "Test"));
        
        SpecialtyResponse response = service.createSpecialty(request);
        
        assertNotNull(response);
    }
    
    @Test
    void delete_ShouldMarkAsDeleted() {
        UUID id = UUID.randomUUID();
        Specialty spec = new Specialty();
        spec.setDeleted(false);
        
        when(repository.findById(id)).thenReturn(Optional.of(spec));
        
        service.delete(id);
        
        assertTrue(spec.isDeleted());
        verify(repository).save(spec);
    }
}
