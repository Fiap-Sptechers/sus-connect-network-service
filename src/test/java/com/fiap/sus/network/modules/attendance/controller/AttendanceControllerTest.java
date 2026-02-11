package com.fiap.sus.network.modules.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.core.exception.SecurityException;
import com.fiap.sus.network.core.security.TokenService;
import com.fiap.sus.network.modules.attendance.client.LiveOpsClient;
import com.fiap.sus.network.modules.attendance.dto.AttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.CompleteAttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.network.modules.attendance.dto.TriageRequest;
import com.fiap.sus.network.modules.attendance.enums.AttendanceStatus;
import com.fiap.sus.network.modules.attendance.enums.RiskClassification;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitResponse;
import com.fiap.sus.network.modules.health_unit.service.HealthUnitService;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import com.fiap.sus.network.modules.user.service.AccessControlService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthUnitService healthUnitService;

    @MockitoBean
    private AccessControlService accessControlService;

    @MockitoBean
    private LiveOpsClient liveOpsClient;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class StartAttendance {

        @Test
        void startAttendance_ShouldReturnCreated_WhenValidRequest() throws Exception {
            UUID unitId = UUID.randomUUID();
            TriageRequest request = new TriageRequest("John Doe", "12345678900", RiskClassification.RED);
            HealthUnitResponse unitResponse = new HealthUnitResponse(unitId, "Hospital Central", "00000000000191", null, null, null);
            AttendanceResponse attendanceResponse = new AttendanceResponse("att-123", "John Doe", AttendanceStatus.WAITING, "2026-02-11T10:00:00");

            when(healthUnitService.findById(unitId)).thenReturn(unitResponse);
            when(liveOpsClient.sendTriage(eq(unitId), any(TriageRequest.class))).thenReturn(attendanceResponse);

            mockMvc.perform(post("/units/{unitId}/attendances", unitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("att-123"))
                    .andExpect(jsonPath("$.patientName").value("John Doe"))
                    .andExpect(jsonPath("$.status").value("WAITING"));

            verify(healthUnitService).findById(unitId);
            verify(liveOpsClient).sendTriage(unitId, request);
        }

        @Test
        void startAttendance_ShouldReturnBadRequest_WhenPatientNameIsBlank() throws Exception {
            UUID unitId = UUID.randomUUID();
            TriageRequest request = new TriageRequest("", "12345678900", RiskClassification.RED);

            mockMvc.perform(post("/units/{unitId}/attendances", unitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(healthUnitService);
            verifyNoInteractions(liveOpsClient);
        }

        @Test
        void startAttendance_ShouldReturnBadRequest_WhenPatientCpfIsBlank() throws Exception {
            UUID unitId = UUID.randomUUID();
            TriageRequest request = new TriageRequest("John Doe", "", RiskClassification.RED);

            mockMvc.perform(post("/units/{unitId}/attendances", unitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(healthUnitService);
            verifyNoInteractions(liveOpsClient);
        }

        @Test
        void startAttendance_ShouldReturnBadRequest_WhenRiskClassificationIsNull() throws Exception {
            UUID unitId = UUID.randomUUID();
            String requestJson = "{\"patientName\":\"John Doe\",\"patientCpf\":\"12345678900\",\"riskClassification\":null}";

            mockMvc.perform(post("/units/{unitId}/attendances", unitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(healthUnitService);
            verifyNoInteractions(liveOpsClient);
        }

        @Test
        void startAttendance_ShouldReturnNotFound_WhenHealthUnitDoesNotExist() throws Exception {
            UUID unitId = UUID.randomUUID();
            TriageRequest request = new TriageRequest("John Doe", "12345678900", RiskClassification.RED);

            when(healthUnitService.findById(unitId)).thenThrow(new ResourceNotFoundException("Health unit not found"));

            mockMvc.perform(post("/units/{unitId}/attendances", unitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(healthUnitService).findById(unitId);
            verifyNoInteractions(liveOpsClient);
        }

        @Test
        void startAttendance_ShouldHandleAllRiskClassifications() throws Exception {
            UUID unitId = UUID.randomUUID();
            HealthUnitResponse unitResponse = new HealthUnitResponse(unitId, "Hospital Central", "00000000000191", null, null, null);

            when(healthUnitService.findById(unitId)).thenReturn(unitResponse);

            for (RiskClassification risk : RiskClassification.values()) {
                TriageRequest request = new TriageRequest("Patient", "12345678900", risk);
                AttendanceResponse response = new AttendanceResponse("att-123", "Patient", AttendanceStatus.WAITING, "2026-02-11T10:00:00");

                when(liveOpsClient.sendTriage(eq(unitId), any(TriageRequest.class))).thenReturn(response);

                mockMvc.perform(post("/units/{unitId}/attendances", unitId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());
            }
        }

    }

    @Nested
    class GetAttendanceById {

        @Test
        void getAttendanceById_ShouldReturnOk_WhenAttendanceExists() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-123";
            CompleteAttendanceResponse response = new CompleteAttendanceResponse(
                    attendanceId, "John Doe", "12345678900", AttendanceStatus.IN_PROGRESS,
                    RiskClassification.RED, "2026-02-11T10:00:00", "2026-02-11T10:15:00", null
            );

            doNothing().when(accessControlService).checkAccess(unitId);
            when(liveOpsClient.getAttendanceById(attendanceId)).thenReturn(response);

            mockMvc.perform(get("/units/{unitId}/attendances/{attendanceId}", unitId, attendanceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(attendanceId))
                    .andExpect(jsonPath("$.patientName").value("John Doe"))
                    .andExpect(jsonPath("$.patientCpf").value("12345678900"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.riskClassification").value("RED"));

            verify(accessControlService).checkAccess(unitId);
            verify(liveOpsClient).getAttendanceById(attendanceId);
        }

        @Test
        void getAttendanceById_ShouldReturnOk_WhenAttendanceIsWaiting() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-456";
            CompleteAttendanceResponse response = new CompleteAttendanceResponse(
                    attendanceId, "Jane Smith", "98765432100", AttendanceStatus.WAITING,
                    RiskClassification.YELLOW, "2026-02-11T11:00:00", null, null
            );

            doNothing().when(accessControlService).checkAccess(unitId);
            when(liveOpsClient.getAttendanceById(attendanceId)).thenReturn(response);

            mockMvc.perform(get("/units/{unitId}/attendances/{attendanceId}", unitId, attendanceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WAITING"))
                    .andExpect(jsonPath("$.startTime").isEmpty());

            verify(accessControlService).checkAccess(unitId);
            verify(liveOpsClient).getAttendanceById(attendanceId);
        }

        @Test
        void getAttendanceById_ShouldReturnOk_WhenAttendanceIsDischarged() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-789";
            CompleteAttendanceResponse response = new CompleteAttendanceResponse(
                    attendanceId, "Bob Johnson", "11122233344", AttendanceStatus.DISCHARGED,
                    RiskClassification.GREEN, "2026-02-11T09:00:00", "2026-02-11T09:30:00", "2026-02-11T10:00:00"
            );

            doNothing().when(accessControlService).checkAccess(unitId);
            when(liveOpsClient.getAttendanceById(attendanceId)).thenReturn(response);

            mockMvc.perform(get("/units/{unitId}/attendances/{attendanceId}", unitId, attendanceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DISCHARGED"))
                    .andExpect(jsonPath("$.dischargeTime").value("2026-02-11T10:00:00"));

            verify(accessControlService).checkAccess(unitId);
            verify(liveOpsClient).getAttendanceById(attendanceId);
        }

        @Test
        void getAttendanceById_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-123";

            doThrow(new SecurityException("Access denied"))
                    .when(accessControlService).checkAccess(unitId);

            mockMvc.perform(get("/units/{unitId}/attendances/{attendanceId}", unitId, attendanceId))
                    .andExpect(status().isForbidden());

            verify(accessControlService).checkAccess(unitId);
            verifyNoInteractions(liveOpsClient);
        }

        @Test
        void getAttendanceById_ShouldReturnNotFound_WhenAttendanceDoesNotExist() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-999";

            doNothing().when(accessControlService).checkAccess(unitId);
            when(liveOpsClient.getAttendanceById(attendanceId))
                    .thenThrow(new ResourceNotFoundException("Attendance not found"));

            mockMvc.perform(get("/units/{unitId}/attendances/{attendanceId}", unitId, attendanceId))
                    .andExpect(status().isNotFound());

            verify(accessControlService).checkAccess(unitId);
            verify(liveOpsClient).getAttendanceById(attendanceId);
        }

    }

    @Nested
    class UpdateStatus {

        @Test
        void updateStatus_ShouldReturnOk_WhenStatusUpdatedSuccessfully() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-123";
            StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.IN_PROGRESS);
            CompleteAttendanceResponse response = new CompleteAttendanceResponse(
                    attendanceId, "John Doe", "12345678900", AttendanceStatus.IN_PROGRESS,
                    RiskClassification.RED, "2026-02-11T10:00:00", "2026-02-11T10:30:00", null
            );

            doNothing().when(accessControlService).checkAccess(unitId);
            when(liveOpsClient.updateStatus(eq(attendanceId), any(StatusUpdateRequest.class))).thenReturn(response);

            mockMvc.perform(patch("/units/{unitId}/attendances/{attendanceId}/status", unitId, attendanceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(attendanceId))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

            verify(accessControlService).checkAccess(unitId);
            verify(liveOpsClient).updateStatus(attendanceId, request);
        }

        @Test
        void updateStatus_ShouldReturnOk_WhenStatusUpdatedToDischarged() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-123";
            StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.DISCHARGED);
            CompleteAttendanceResponse response = new CompleteAttendanceResponse(
                    attendanceId, "John Doe", "12345678900", AttendanceStatus.DISCHARGED,
                    RiskClassification.RED, "2026-02-11T10:00:00", "2026-02-11T10:30:00", "2026-02-11T11:00:00"
            );

            doNothing().when(accessControlService).checkAccess(unitId);
            when(liveOpsClient.updateStatus(eq(attendanceId), any(StatusUpdateRequest.class))).thenReturn(response);

            mockMvc.perform(patch("/units/{unitId}/attendances/{attendanceId}/status", unitId, attendanceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DISCHARGED"))
                    .andExpect(jsonPath("$.dischargeTime").value("2026-02-11T11:00:00"));

            verify(accessControlService).checkAccess(unitId);
            verify(liveOpsClient).updateStatus(attendanceId, request);
        }

        @Test
        void updateStatus_ShouldHandleAllStatusTransitions() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-123";

            doNothing().when(accessControlService).checkAccess(unitId);

            for (AttendanceStatus status : AttendanceStatus.values()) {
                StatusUpdateRequest request = new StatusUpdateRequest(status);
                CompleteAttendanceResponse response = new CompleteAttendanceResponse(
                        attendanceId, "Patient", "12345678900", status,
                        RiskClassification.YELLOW, "2026-02-11T10:00:00", "2026-02-11T10:30:00", null
                );

                when(liveOpsClient.updateStatus(eq(attendanceId), any(StatusUpdateRequest.class))).thenReturn(response);

                mockMvc.perform(patch("/units/{unitId}/attendances/{attendanceId}/status", unitId, attendanceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value(status.name()));
            }
        }

        @Test
        void updateStatus_ShouldReturnBadRequest_WhenNewStatusIsNull() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-123";
            String requestJson = "{\"newStatus\":null}";

            mockMvc.perform(patch("/units/{unitId}/attendances/{attendanceId}/status", unitId, attendanceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(accessControlService);
            verifyNoInteractions(liveOpsClient);
        }

        @Test
        void updateStatus_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-123";
            StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.IN_PROGRESS);

            doThrow(new SecurityException("Access denied"))
                    .when(accessControlService).checkAccess(unitId);

            mockMvc.perform(patch("/units/{unitId}/attendances/{attendanceId}/status", unitId, attendanceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(accessControlService).checkAccess(unitId);
            verifyNoInteractions(liveOpsClient);
        }

        @Test
        void updateStatus_ShouldReturnNotFound_WhenAttendanceDoesNotExist() throws Exception {
            UUID unitId = UUID.randomUUID();
            String attendanceId = "att-999";
            StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.IN_PROGRESS);

            doNothing().when(accessControlService).checkAccess(unitId);
            when(liveOpsClient.updateStatus(eq(attendanceId), any(StatusUpdateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Attendance not found"));

            mockMvc.perform(patch("/units/{unitId}/attendances/{attendanceId}/status", unitId, attendanceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(accessControlService).checkAccess(unitId);
            verify(liveOpsClient).updateStatus(attendanceId, request);
        }

    }

}

