package com.fiap.sus.network.modules.doctor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.sus.network.modules.doctor.dto.DoctorRequest;
import com.fiap.sus.network.modules.doctor.dto.DoctorResponse;
import com.fiap.sus.network.modules.doctor.service.DoctorService;
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
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@AutoConfigureMockMvc(addFilters = false)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService service;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listDoctors_ShouldReturnOk() throws Exception {
        when(service.listDoctors()).thenReturn(List.of());

        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createDoctor_ShouldReturnCreated() throws Exception {
        DoctorRequest request = new DoctorRequest("Dr. Test", "CRM/SP 123456", Set.of());
        DoctorResponse response = new DoctorResponse(UUID.randomUUID(), "Dr. Test", "CRM/SP 123456", Set.of());

        when(service.createDoctor(any())).thenReturn(response);

        mockMvc.perform(post("/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dr. Test"));
    }

    @Test
    void getById_ShouldReturnDoctor() throws Exception {
        UUID id = UUID.randomUUID();
        DoctorResponse response = new DoctorResponse(id, "Dr. Test", "CRM/SP 123456", Set.of());

        when(service.findById(id)).thenReturn(response);

        mockMvc.perform(get("/doctors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/doctors/{id}", id))
                .andExpect(status().isNoContent());
    }
}
