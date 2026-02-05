package com.fiap.sus.network.modules.health_unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitRequest;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitResponse;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitStatusResponse;
import com.fiap.sus.network.modules.health_unit.service.HealthUnitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.fiap.sus.network.core.security.TokenService;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthUnitController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthUnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthUnitService service;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_ShouldReturnCreated() throws Exception {
        HealthUnitRequest request = new HealthUnitRequest("Unit 1", "00000000000191", null, null);
        HealthUnitResponse response = new HealthUnitResponse(UUID.randomUUID(), "Unit 1", "00000000000191", null, null);

        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Unit 1"));
    }

    @Test
    void list_ShouldReturnOk() throws Exception {
        when(service.findAll(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/units"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ShouldReturnUnit() throws Exception {
        UUID id = UUID.randomUUID();
        HealthUnitResponse response = new HealthUnitResponse(id, "Unit 1", "00000000000191", null, null);

        when(service.findById(id)).thenReturn(response);

        mockMvc.perform(get("/units/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getStatus_ShouldReturnStatus() throws Exception {
        UUID id = UUID.randomUUID();
        HealthUnitStatusResponse response = new HealthUnitStatusResponse(id, "Unit 1", List.of());

        when(service.getStatus(id)).thenReturn(response);

        mockMvc.perform(get("/units/{id}/status", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Unit 1"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/units/{id}", id))
                .andExpect(status().isNoContent());
    }
}
