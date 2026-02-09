package com.fiap.sus.network.modules.shift.mapper;

import com.fiap.sus.network.modules.doctor.mapper.DoctorMapper;
import com.fiap.sus.network.modules.shift.dto.ShiftResponse;
import com.fiap.sus.network.modules.shift.entity.Shift;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import com.fiap.sus.network.modules.specialty.mapper.SpecialtyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftMapperTest {

    @Mock
    private SpecialtyMapper specialtyMapper;
    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private ShiftMapper mapper;

    @Test
    void toDto_ShouldMapEntityToDto() {
        Shift entity = new Shift();
        entity.setId(UUID.randomUUID());
        entity.setUnitId(UUID.randomUUID());
        entity.setSpecialty(new Specialty());
        entity.setCapacity(50);
        entity.setDoctors(Collections.emptySet());

        when(specialtyMapper.toDto(any())).thenReturn(null);

        ShiftResponse dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.id());
        assertEquals(50, dto.capacity());
    }

    @Test
    void toDto_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(mapper.toDto(null));
    }
}
