package com.fiap.sus.network.modules.health_unit.mapper;

import com.fiap.sus.network.modules.health_unit.dto.AddressResponse;
import com.fiap.sus.network.modules.health_unit.entity.Address;
import org.springframework.stereotype.Component;

import com.fiap.sus.network.modules.health_unit.dto.AddressRequest;

@Component
public class AddressMapper {
    public AddressResponse toDto(Address entity) {
        if (entity == null) return null;
        return new AddressResponse(
            entity.getStreet(),
            entity.getNumber(),
            entity.getComplement(),
            entity.getNeighborhood(),
            entity.getCity(),
            entity.getState(),
            entity.getZipCode()
        );
    }

    public Address toEntity(AddressRequest request) {
        if (request == null) return null;
        Address entity = new Address();
        entity.setStreet(request.street());
        entity.setNumber(request.number());
        entity.setComplement(request.complement());
        entity.setNeighborhood(request.neighborhood());
        entity.setCity(request.city());
        entity.setState(request.state());
        entity.setZipCode(request.zipCode());
        return entity;
    }
}
