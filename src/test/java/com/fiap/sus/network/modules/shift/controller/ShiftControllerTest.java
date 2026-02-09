package com.fiap.sus.network.modules.shift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.sus.network.core.security.TokenService;
import com.fiap.sus.network.modules.shift.dto.ShiftScheduleRequest;
import com.fiap.sus.network.modules.shift.dto.ShiftUpdateRequest;
import com.fiap.sus.network.modules.shift.service.ShiftService;
import com.fiap.sus.network.modules.specialty.enums.SpecialtyEnum;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShiftController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftService service;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void update_ShouldReturnOk() throws Exception {
        ShiftUpdateRequest request = new ShiftUpdateRequest(UUID.randomUUID(), SpecialtyEnum.CLINICA_GERAL, 10);
        doNothing().when(service).updateShift(any());

        mockMvc.perform(put("/shifts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void defineSchedule_ShouldReturnCreated() throws Exception {
        ShiftScheduleRequest request = new ShiftScheduleRequest(UUID.randomUUID(), SpecialtyEnum.CLINICA_GERAL, List.of("CRM/SP 123456"));
        doNothing().when(service).defineSchedule(any());

        mockMvc.perform(post("/shifts/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
