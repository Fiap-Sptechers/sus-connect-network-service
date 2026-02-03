package com.fiap.sus.network.modules.health_unit.mapper;

import com.fiap.sus.network.modules.health_unit.dto.HealthUnitRequest;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitResponse;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mapstruct.factory.Mappers;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HealthUnitMapperTest {
    
    private HealthUnitMapper mapper;

    
    @BeforeEach
    void setUp() {
         AddressMapper am = mock(AddressMapper.class); // Mock dependency
         mapper = new HealthUnitMapper(am);
    }

    @Test
    void toEntity_ShouldMapFields() {
        HealthUnitRequest request = new HealthUnitRequest("Unit 1", "CNPJ", null, null);
        HealthUnit entity = mapper.toEntity(request);
        
        assertEquals("Unit 1", entity.getName());
        assertEquals("CNPJ", entity.getCnpj());
    }

    @Test
    void toDto_ShouldMapFields() {
        HealthUnit entity = new HealthUnit();
        entity.setId(UUID.randomUUID());
        entity.setName("Unit 1");
        entity.setCnpj("CNPJ");
        entity.setContacts(java.util.Collections.emptyList());
        
        HealthUnitResponse response = mapper.toDto(entity);
        
        assertEquals(entity.getId(), response.id());
        assertEquals(entity.getName(), response.name());
        assertEquals(entity.getCnpj(), response.cnpj());
    }
}
