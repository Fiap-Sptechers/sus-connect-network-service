package com.fiap.sus.network.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.sus.network.core.security.TokenService;
import com.fiap.sus.network.modules.health_unit.repository.HealthUnitRepository;
import com.fiap.sus.network.modules.user.dto.MemberRequest;
import com.fiap.sus.network.modules.user.mapper.UserMapper;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import com.fiap.sus.network.modules.user.service.AccessControlService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnitMembersController.class)
@AutoConfigureMockMvc(addFilters = false)
class UnitMembersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccessControlService accessControlService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private HealthUnitRepository unitSaudeRepository;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listMembers_ShouldReturnOk() throws Exception {
        UUID unitId = UUID.randomUUID();
        when(accessControlService.listMembers(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/units/{unitId}/users", unitId))
                .andExpect(status().isOk());
    }

    @Test
    void addMember_ShouldReturnOk() throws Exception {
        UUID unitId = UUID.randomUUID();
        MemberRequest request = new MemberRequest(UUID.randomUUID(), "MANAGER");
        doNothing().when(accessControlService).addMember(any(), any(), any(), any());

        mockMvc.perform(post("/units/{unitId}/users", unitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
