package com.fiap.sus.network.modules.attendance.client;

import com.fiap.sus.network.core.exception.ExternalServiceException;
import com.fiap.sus.network.core.security.M2MTokenService;
import com.fiap.sus.network.modules.attendance.dto.AttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.CompleteAttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.network.modules.attendance.dto.TriageRequest;
import com.fiap.sus.network.modules.attendance.dto.liveops.LiveOpsAttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.liveops.LiveOpsCompleteAttendanceResponse;
import com.fiap.sus.network.modules.attendance.enums.AttendanceStatus;
import com.fiap.sus.network.modules.attendance.enums.RiskClassification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class LiveOpsClientTest {

    @Mock
    private M2MTokenService tokenService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private LiveOpsClient liveOpsClient;

    private static final String LIVEOPS_URL = "http://liveops-api.test";
    private static final String TEST_TOKEN = "test-m2m-token";

    @BeforeEach
    void setUp() {
        liveOpsClient = new LiveOpsClient(tokenService);
        ReflectionTestUtils.setField(liveOpsClient, "restClient", restClient);
        ReflectionTestUtils.setField(liveOpsClient, "liveOpsUrl", LIVEOPS_URL);
    }

    @Nested
    class SendTriage {

        @Test
        void sendTriage_ShouldReturnAttendanceResponse_WhenSuccessful() {
            UUID unitId = UUID.randomUUID();
            TriageRequest request = new TriageRequest("John Doe", "12345678900", RiskClassification.RED);
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LiveOpsAttendanceResponse liveOpsResponse = new LiveOpsAttendanceResponse(
                    "att-123", unitId.toString(), "John Doe", AttendanceStatus.WAITING, RiskClassification.RED, entryTime
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(LIVEOPS_URL + "/attendances/triage")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsAttendanceResponse.class)).thenReturn(liveOpsResponse);

            AttendanceResponse result = liveOpsClient.sendTriage(unitId, request);

            assertNotNull(result);
            assertEquals("att-123", result.id());
            assertEquals("John Doe", result.patientName());
            assertEquals(AttendanceStatus.WAITING, result.status());
            assertEquals(String.valueOf(entryTime), result.entryTime());
            verify(tokenService).generateToken();
        }

        @Test
        void sendTriage_ShouldThrowException_WhenResponseIsNull() {
            UUID unitId = UUID.randomUUID();
            TriageRequest request = new TriageRequest("John Doe", "12345678900", RiskClassification.RED);

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(LIVEOPS_URL + "/attendances/triage")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            lenient().when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsAttendanceResponse.class)).thenReturn(null);

            ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                    () -> liveOpsClient.sendTriage(unitId, request));

            assertEquals("LiveOps API did not return a response", exception.getMessage());
            verify(tokenService).generateToken();
        }

        @Test
        void sendTriage_ShouldHandleAllRiskClassifications() {
            UUID unitId = UUID.randomUUID();
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            lenient().when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);

            for (RiskClassification risk : RiskClassification.values()) {
                TriageRequest request = new TriageRequest("Patient", "12345678900", risk);
                LiveOpsAttendanceResponse liveOpsResponse = new LiveOpsAttendanceResponse(
                        "att-123", unitId.toString(), "Patient", AttendanceStatus.WAITING, risk, entryTime
                );
                when(responseSpec.body(LiveOpsAttendanceResponse.class)).thenReturn(liveOpsResponse);

                AttendanceResponse result = liveOpsClient.sendTriage(unitId, request);

                assertNotNull(result);
                assertEquals("att-123", result.id());
            }
        }

        @Test
        void sendTriage_ShouldUseCorrectEndpoint() {
            UUID unitId = UUID.randomUUID();
            TriageRequest request = new TriageRequest("Patient", "12345678900", RiskClassification.BLUE);
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LiveOpsAttendanceResponse liveOpsResponse = new LiveOpsAttendanceResponse(
                    "att-123", unitId.toString(), "Patient", AttendanceStatus.WAITING, RiskClassification.BLUE, entryTime
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(LIVEOPS_URL + "/attendances/triage")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            lenient().when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsAttendanceResponse.class)).thenReturn(liveOpsResponse);

            liveOpsClient.sendTriage(unitId, request);

            verify(requestBodyUriSpec).uri(LIVEOPS_URL + "/attendances/triage");
        }

    }

    @Nested
    class GetAttendanceById {

        @Test
        void getAttendanceById_ShouldReturnCompleteResponse_WhenAttendanceExists() {
            String attendanceId = "att-123";
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LocalDateTime startTime = LocalDateTime.of(2026, 2, 11, 10, 15);
            LocalDateTime dischargeTime = LocalDateTime.of(2026, 2, 11, 11, 0);

            LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                    attendanceId, UUID.randomUUID().toString(), "John Doe", "12345678900",
                    AttendanceStatus.DISCHARGED, RiskClassification.RED, entryTime, startTime, dischargeTime
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

            CompleteAttendanceResponse result = liveOpsClient.getAttendanceById(attendanceId);

            assertNotNull(result);
            assertEquals(attendanceId, result.id());
            assertEquals("John Doe", result.patientName());
            assertEquals("12345678900", result.patientCpf());
            assertEquals(AttendanceStatus.DISCHARGED, result.status());
            assertEquals(RiskClassification.RED, result.riskClassification());
            assertEquals(String.valueOf(entryTime), result.entryTime());
            assertEquals(String.valueOf(startTime), result.startTime());
            assertEquals(String.valueOf(dischargeTime), result.dischargeTime());
            verify(tokenService).generateToken();
        }

        @Test
        void getAttendanceById_ShouldReturnResponse_WhenAttendanceIsWaiting() {
            String attendanceId = "att-456";
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 11, 0);

            LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                    attendanceId, UUID.randomUUID().toString(), "Jane Smith", "98765432100",
                    AttendanceStatus.WAITING, RiskClassification.YELLOW, entryTime, null, null
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

            CompleteAttendanceResponse result = liveOpsClient.getAttendanceById(attendanceId);

            assertNotNull(result);
            assertEquals(attendanceId, result.id());
            assertEquals(AttendanceStatus.WAITING, result.status());
            assertEquals("null", result.startTime());
            assertEquals("null", result.dischargeTime());
        }

        @Test
        void getAttendanceById_ShouldReturnResponse_WhenAttendanceIsInProgress() {
            String attendanceId = "att-789";
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 9, 0);
            LocalDateTime startTime = LocalDateTime.of(2026, 2, 11, 9, 30);

            LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                    attendanceId, UUID.randomUUID().toString(), "Bob Johnson", "11122233344",
                    AttendanceStatus.IN_PROGRESS, RiskClassification.GREEN, entryTime, startTime, null
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

            CompleteAttendanceResponse result = liveOpsClient.getAttendanceById(attendanceId);

            assertNotNull(result);
            assertEquals(attendanceId, result.id());
            assertEquals(AttendanceStatus.IN_PROGRESS, result.status());
            assertEquals(String.valueOf(startTime), result.startTime());
            assertEquals("null", result.dischargeTime());
        }

        @Test
        void getAttendanceById_ShouldThrowException_WhenResponseIsNull() {
            String attendanceId = "att-999";

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(null);

            ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                    () -> liveOpsClient.getAttendanceById(attendanceId));

            assertEquals("LiveOps API did not return a response", exception.getMessage());
            verify(tokenService).generateToken();
        }

        @Test
        void getAttendanceById_ShouldHandleAllRiskClassifications() {
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LocalDateTime startTime = LocalDateTime.of(2026, 2, 11, 10, 15);

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

            for (RiskClassification risk : RiskClassification.values()) {
                String attendanceId = "att-" + risk.name();
                LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                        attendanceId, UUID.randomUUID().toString(), "Patient", "12345678900",
                        AttendanceStatus.IN_PROGRESS, risk, entryTime, startTime, null
                );
                when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

                CompleteAttendanceResponse result = liveOpsClient.getAttendanceById(attendanceId);

                assertNotNull(result);
                assertEquals(risk, result.riskClassification());
            }
        }

        @Test
        void getAttendanceById_ShouldUseCorrectEndpoint() {
            String attendanceId = "att-specific-id";
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                    attendanceId, UUID.randomUUID().toString(), "Patient", "12345678900",
                    AttendanceStatus.WAITING, RiskClassification.GREEN, entryTime, null, null
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

            liveOpsClient.getAttendanceById(attendanceId);

            verify(requestHeadersUriSpec).uri(LIVEOPS_URL + "/attendances/" + attendanceId);
        }

    }

    @Nested
    class UpdateStatus {

        @Test
        void updateStatus_ShouldReturnUpdatedResponse_WhenStatusUpdatedToInProgress() {
            String attendanceId = "att-123";
            StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.IN_PROGRESS);
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LocalDateTime startTime = LocalDateTime.of(2026, 2, 11, 10, 30);

            LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                    attendanceId, UUID.randomUUID().toString(), "John Doe", "12345678900",
                    AttendanceStatus.IN_PROGRESS, RiskClassification.RED, entryTime, startTime, null
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.patch()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId + "/status")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

            CompleteAttendanceResponse result = liveOpsClient.updateStatus(attendanceId, request);

            assertNotNull(result);
            assertEquals(attendanceId, result.id());
            assertEquals(AttendanceStatus.IN_PROGRESS, result.status());
            assertEquals(String.valueOf(startTime), result.startTime());
            assertEquals("null", result.dischargeTime());
            verify(tokenService).generateToken();
        }

        @Test
        void updateStatus_ShouldReturnUpdatedResponse_WhenStatusUpdatedToDischarged() {
            String attendanceId = "att-123";
            StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.DISCHARGED);
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LocalDateTime startTime = LocalDateTime.of(2026, 2, 11, 10, 30);
            LocalDateTime dischargeTime = LocalDateTime.of(2026, 2, 11, 11, 0);

            LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                    attendanceId, UUID.randomUUID().toString(), "John Doe", "12345678900",
                    AttendanceStatus.DISCHARGED, RiskClassification.RED, entryTime, startTime, dischargeTime
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.patch()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId + "/status")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

            CompleteAttendanceResponse result = liveOpsClient.updateStatus(attendanceId, request);

            assertNotNull(result);
            assertEquals(attendanceId, result.id());
            assertEquals(AttendanceStatus.DISCHARGED, result.status());
            assertEquals(String.valueOf(dischargeTime), result.dischargeTime());
            verify(tokenService).generateToken();
        }

        @Test
        void updateStatus_ShouldHandleAllStatusTransitions() {
            String attendanceId = "att-123";
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LocalDateTime startTime = LocalDateTime.of(2026, 2, 11, 10, 15);
            LocalDateTime dischargeTime = LocalDateTime.of(2026, 2, 11, 11, 0);

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.patch()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            lenient().when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);

            for (AttendanceStatus status : AttendanceStatus.values()) {
                StatusUpdateRequest request = new StatusUpdateRequest(status);
                LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                        attendanceId, UUID.randomUUID().toString(), "Patient", "12345678900",
                        status, RiskClassification.YELLOW, entryTime, startTime, dischargeTime
                );
                when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

                CompleteAttendanceResponse result = liveOpsClient.updateStatus(attendanceId, request);

                assertNotNull(result);
                assertEquals(status, result.status());
            }
        }

        @Test
        void updateStatus_ShouldThrowException_WhenResponseIsNull() {
            String attendanceId = "att-999";
            StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.IN_PROGRESS);

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.patch()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId + "/status")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(null);

            ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                    () -> liveOpsClient.updateStatus(attendanceId, request));

            assertEquals("LiveOps API did not return a response", exception.getMessage());
            verify(tokenService).generateToken();
        }

        @Test
        void updateStatus_ShouldPreservePatientInformation() {
            String attendanceId = "att-123";
            String patientName = "Maria Silva";
            String patientCpf = "99988877766";
            StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.IN_PROGRESS);
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LocalDateTime startTime = LocalDateTime.of(2026, 2, 11, 10, 30);

            LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                    attendanceId, UUID.randomUUID().toString(), patientName, patientCpf,
                    AttendanceStatus.IN_PROGRESS, RiskClassification.ORANGE, entryTime, startTime, null
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.patch()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId + "/status")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            doReturn(requestBodySpec).when(requestBodySpec).body(request);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

            CompleteAttendanceResponse result = liveOpsClient.updateStatus(attendanceId, request);

            assertNotNull(result);
            assertEquals(patientName, result.patientName());
            assertEquals(patientCpf, result.patientCpf());
            verify(tokenService).generateToken();
        }

        @Test
        void updateStatus_ShouldUseCorrectEndpoint() {
            String attendanceId = "att-update-id";
            StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.IN_PROGRESS);
            LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);
            LocalDateTime startTime = LocalDateTime.of(2026, 2, 11, 10, 30);
            LiveOpsCompleteAttendanceResponse liveOpsResponse = new LiveOpsCompleteAttendanceResponse(
                    attendanceId, UUID.randomUUID().toString(), "Patient", "12345678900",
                    AttendanceStatus.IN_PROGRESS, RiskClassification.YELLOW, entryTime, startTime, null
            );

            when(tokenService.generateToken()).thenReturn(TEST_TOKEN);
            when(restClient.patch()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(LIVEOPS_URL + "/attendances/" + attendanceId + "/status")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(liveOpsResponse);

            liveOpsClient.updateStatus(attendanceId, request);

            verify(requestBodyUriSpec).uri(LIVEOPS_URL + "/attendances/" + attendanceId + "/status");
        }

    }

    @Test
    void allMethods_ShouldIncludeAuthorizationHeader() {
        UUID unitId = UUID.randomUUID();
        String attendanceId = "att-123";
        LocalDateTime entryTime = LocalDateTime.of(2026, 2, 11, 10, 0);

        when(tokenService.generateToken()).thenReturn(TEST_TOKEN);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(LiveOpsAttendanceResponse.class)).thenReturn(
                new LiveOpsAttendanceResponse("att-123", unitId.toString(), "Patient",
                        AttendanceStatus.WAITING, RiskClassification.RED, entryTime)
        );

        liveOpsClient.sendTriage(unitId, new TriageRequest("Patient", "12345678900", RiskClassification.RED));
        verify(requestBodySpec).header("Authorization", "Bearer " + TEST_TOKEN);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(LiveOpsCompleteAttendanceResponse.class)).thenReturn(
                new LiveOpsCompleteAttendanceResponse(attendanceId, unitId.toString(), "Patient", "12345678900",
                        AttendanceStatus.WAITING, RiskClassification.RED, entryTime, null, null)
        );

        liveOpsClient.getAttendanceById(attendanceId);
        verify(requestHeadersSpec).header("Authorization", "Bearer " + TEST_TOKEN);

        when(restClient.patch()).thenReturn(requestBodyUriSpec);

        liveOpsClient.updateStatus(attendanceId, new StatusUpdateRequest(AttendanceStatus.IN_PROGRESS));
        verify(requestBodySpec, times(2)).header("Authorization", "Bearer " + TEST_TOKEN);
    }

}

