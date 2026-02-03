package com.fiap.sus.network.modules.specialty.mapper;

import com.fiap.sus.network.modules.specialty.dto.SpecialtyResponse;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import org.springframework.stereotype.Component;

@Component
public class SpecialtyMapper {
    public SpecialtyResponse toDto(Specialty entity) {
        if (entity == null) return null;
        return new SpecialtyResponse(entity.getId(), entity.getName());
    }
}
