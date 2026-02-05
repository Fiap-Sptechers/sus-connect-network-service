package com.fiap.sus.network.modules.health_unit.mapper;

import com.fiap.sus.network.modules.health_unit.dto.AddressResponse;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitRequest;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitResponse;
import com.fiap.sus.network.modules.health_unit.entity.Address;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthUnitMapperTest {

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private HealthUnitMapper mapper;

    @Test
    void toDto_ShouldMapFields() {
        HealthUnit unit = new HealthUnit();
        unit.setId(UUID.randomUUID());
        unit.setName("Test");
        unit.setCnpj("123");
        unit.setContacts(new ArrayList<>());

        when(addressMapper.toDto(any())).thenReturn(null);

        HealthUnitResponse response = mapper.toDto(unit);

        assertNotNull(response);
        assertEquals(unit.getName(), response.name());
        assertEquals(unit.getCnpj(), response.cnpj());
    }

    @Test
    void toEntity_ShouldMapFields() {
        com.fiap.sus.network.modules.health_unit.dto.ContactRequest cReq = new com.fiap.sus.network.modules.health_unit.dto.ContactRequest(com.fiap.sus.network.modules.health_unit.entity.ContactType.PHONE, "123", "Desc");
        HealthUnitRequest request = new HealthUnitRequest("Test", "123", null, java.util.List.of(cReq));

        when(addressMapper.toEntity(any())).thenReturn(new Address());

        HealthUnit entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(request.name(), entity.getName());
        assertEquals(request.cnpj(), entity.getCnpj());
        assertEquals(1, entity.getContacts().size());
    }

    @Test
    void toDto_ShouldReturnNull_WhenInputIsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toEntity_ShouldReturnNull_WhenInputIsNull() {
        assertNull(mapper.toEntity(null));
    }
}
