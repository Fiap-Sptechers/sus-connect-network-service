package com.fiap.sus.network.modules.doctor.mapper;

import com.fiap.sus.network.modules.doctor.dto.DoctorResponse;
import com.fiap.sus.network.modules.doctor.entity.Doctor;
import com.fiap.sus.network.modules.specialty.mapper.SpecialtyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DoctorMapperTest {

    @Mock
    private SpecialtyMapper specialtyMapper;

    @InjectMocks
    private DoctorMapper mapper;

    @Test
    void toDto_ShouldMapFields() {
        Doctor doctor = new Doctor();
        doctor.setId(UUID.randomUUID());
        doctor.setName("Dr. Test");
        doctor.setCrm("CRM123");
        doctor.setSpecialties(new HashSet<>());

        DoctorResponse response = mapper.toDto(doctor);

        assertNotNull(response);
        assertEquals(doctor.getName(), response.name());
        assertEquals(doctor.getCrm(), response.crm());
    }

    @Test
    void toDto_ShouldReturnNull_WhenInputIsNull() {
        assertNull(mapper.toDto(null));
    }
}
