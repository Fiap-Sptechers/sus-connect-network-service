package com.fiap.sus.network.modules.shift.mapper;

import com.fiap.sus.network.modules.specialty.mapper.SpecialtyMapper;
import com.fiap.sus.network.modules.doctor.mapper.DoctorMapper;

import com.fiap.sus.network.modules.shift.dto.ShiftResponse;
import com.fiap.sus.network.modules.shift.entity.Shift;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.Set;
import com.fiap.sus.network.modules.doctor.dto.DoctorResponse;

@Component
@RequiredArgsConstructor
public class ShiftMapper {

    private final SpecialtyMapper specialtyMapper;
    private final DoctorMapper doctorMapper;

    public ShiftResponse toDto(Shift entity) {
        if (entity == null) return null;
        
        Set<DoctorResponse> doctorsDto = entity.getDoctors().stream()
                .map(doctorMapper::toDto)
                .collect(Collectors.toSet());

        return new ShiftResponse(
            entity.getId(), 
            entity.getUnitId(), 
            specialtyMapper.toDto(entity.getSpecialty()), 
            entity.getCapacity(),
            entity.getActiveDoctors(),
            doctorsDto
        );
    }
}
