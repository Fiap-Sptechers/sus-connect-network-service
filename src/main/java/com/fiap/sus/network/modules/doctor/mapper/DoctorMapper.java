package com.fiap.sus.network.modules.doctor.mapper;

import com.fiap.sus.network.modules.specialty.mapper.SpecialtyMapper;

import com.fiap.sus.network.modules.doctor.dto.DoctorResponse;
import com.fiap.sus.network.modules.doctor.entity.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.Set;
import com.fiap.sus.network.modules.specialty.dto.SpecialtyResponse;

@Component
@RequiredArgsConstructor
public class DoctorMapper {

    private final SpecialtyMapper specialtyMapper;

    public DoctorResponse toDto(Doctor entity) {
        if (entity == null) return null;
        Set<SpecialtyResponse> specs = entity.getSpecialties().stream()
                .map(specialtyMapper::toDto)
                .collect(Collectors.toSet());
        return new DoctorResponse(entity.getId(), entity.getName(), entity.getCrm(), specs);
    }
}
