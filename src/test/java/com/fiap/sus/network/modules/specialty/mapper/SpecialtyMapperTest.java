package com.fiap.sus.network.modules.specialty.mapper;

import com.fiap.sus.network.modules.specialty.dto.SpecialtyResponse;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpecialtyMapperTest {

    private final SpecialtyMapper mapper = new SpecialtyMapper();

    @Test
    void toDto_ShouldMapFields() {
        Specialty specialty = new Specialty();
        specialty.setId(UUID.randomUUID());
        specialty.setName("Cardiology");

        SpecialtyResponse response = mapper.toDto(specialty);

        assertNotNull(response);
        assertEquals(specialty.getName(), response.name());
        assertEquals(specialty.getId(), response.id());
    }

    @Test
    void toDto_ShouldReturnNull_WhenInputIsNull() {
        assertNull(mapper.toDto(null));
    }
}
