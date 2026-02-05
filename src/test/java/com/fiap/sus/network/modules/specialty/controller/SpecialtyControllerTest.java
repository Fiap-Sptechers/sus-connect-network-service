package com.fiap.sus.network.modules.specialty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.sus.network.modules.specialty.dto.SpecialtyRequest;
import com.fiap.sus.network.modules.specialty.dto.SpecialtyResponse;
import com.fiap.sus.network.modules.specialty.service.SpecialtyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.fiap.sus.network.core.security.TokenService;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpecialtyController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpecialtyService service;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listSpecialties_ShouldReturnOk() throws Exception {
        when(service.listSpecialties()).thenReturn(List.of());

        mockMvc.perform(get("/specialties"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createSpecialty_ShouldReturnCreated() throws Exception {
        SpecialtyRequest request = new SpecialtyRequest("Cardiology");
        SpecialtyResponse response = new SpecialtyResponse(UUID.randomUUID(), "Cardiology");

        when(service.createSpecialty(any())).thenReturn(response);

        mockMvc.perform(post("/specialties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cardiology"));
    }

    @Test
    void getById_ShouldReturnSpecialty() throws Exception {
        UUID id = UUID.randomUUID();
        SpecialtyResponse response = new SpecialtyResponse(id, "Cardiology");

        when(service.findById(id)).thenReturn(response);

        mockMvc.perform(get("/specialties/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/specialties/{id}", id))
                .andExpect(status().isNoContent());
    }
}
