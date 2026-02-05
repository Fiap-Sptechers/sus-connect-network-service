package com.fiap.sus.network.modules.health_unit.mapper;

import com.fiap.sus.network.modules.health_unit.dto.AddressRequest;
import com.fiap.sus.network.modules.health_unit.dto.AddressResponse;
import com.fiap.sus.network.modules.health_unit.entity.Address;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressMapperTest {

    private final AddressMapper mapper = new AddressMapper();

    @Test
    void toDto_ShouldMapEntityToDto() {
        Address entity = new Address();
        entity.setStreet("Street 1");
        entity.setNumber("123");
        entity.setZipCode("12345678");

        AddressResponse dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals("Street 1", dto.street());
        assertEquals("123", dto.number());
        assertEquals("12345678", dto.zipCode());
    }

    @Test
    void toDto_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toEntity_ShouldMapDtoToEntity() {
        AddressRequest request = new AddressRequest("Street 1", "123", "Comp", "Neigh", "City", "ST", "12345678");

        Address entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("Street 1", entity.getStreet());
        assertEquals("123", entity.getNumber());
        assertEquals("12345678", entity.getZipCode());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenRequestIsNull() {
        assertNull(mapper.toEntity(null));
    }
}
