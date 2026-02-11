package com.fiap.sus.network.modules.attendance.client;

import com.fiap.sus.network.core.exception.ExternalServiceException;
import com.fiap.sus.network.core.security.M2MTokenService;
import com.fiap.sus.network.modules.attendance.dto.AttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.CompleteAttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.network.modules.attendance.dto.liveops.LiveOpsAttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.liveops.LiveOpsCompleteAttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.liveops.LiveOpsTriageRequest;
import com.fiap.sus.network.modules.attendance.dto.TriageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveOpsClient {

    private final M2MTokenService tokenService;
    private final RestClient restClient = RestClient.create();

    @Value("${app.liveops.url}")
    private String liveOpsUrl;

    public AttendanceResponse sendTriage(UUID unitId, TriageRequest req) {
        log.info("Sending new attendance to LiveOps API for Health Unit ID: {}", unitId);

        String token = tokenService.generateToken();
        LiveOpsTriageRequest liveOpsReq = new LiveOpsTriageRequest(
                unitId,
                req.patientName(),
                req.patientCpf(),
                req.riskClassification()
        );

        LiveOpsAttendanceResponse response = restClient
                .post()
                .uri(liveOpsUrl + "/attendances/triage")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(liveOpsReq)
                .retrieve()
                .body(LiveOpsAttendanceResponse.class);

        if (response == null) {
            log.error("Failed to receive response from LiveOps API for Health Unit ID: {}", unitId);
            throw new ExternalServiceException("LiveOps API did not return a response");
        }

        log.info("Triage sent successfully for Health Unit ID: {} - Attendance ID: {}", unitId, response.id());

        return new AttendanceResponse(
                response.id(),
                response.patientName(),
                response.status(),
                String.valueOf(response.entryTime())
        );
    }

    public CompleteAttendanceResponse getAttendanceById(String attendanceId) {
        String token = tokenService.generateToken();

        LiveOpsCompleteAttendanceResponse response = restClient
                .get()
                .uri(liveOpsUrl + "/attendances/" + attendanceId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(LiveOpsCompleteAttendanceResponse.class);

        if (response == null) {
            log.error("Failed to receive response from LiveOps API while searching for Attendance ID: {}", attendanceId);
            throw new ExternalServiceException("LiveOps API did not return a response");
        }

        return new CompleteAttendanceResponse(
                response.id(),
                response.patientName(),
                response.patientCpf(),
                response.status(),
                response.riskClassification(),
                String.valueOf(response.entryTime()),
                String.valueOf(response.startTime()),
                String.valueOf(response.dischargeTime())
        );
    }

    public CompleteAttendanceResponse updateStatus(String attendanceId, @Valid StatusUpdateRequest request) {

        String token = tokenService.generateToken();
        LiveOpsCompleteAttendanceResponse response = restClient
                .patch()
                .uri(liveOpsUrl + "/attendances/" + attendanceId + "/status")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LiveOpsCompleteAttendanceResponse.class);

        if (response == null) {
            log.error("Failed to receive response from LiveOps API  while updating status for Attendance ID: {}", attendanceId);
            throw new ExternalServiceException("LiveOps API did not return a response");
        }

        return new CompleteAttendanceResponse(
                response.id(),
                response.patientName(),
                response.patientCpf(),
                response.status(),
                response.riskClassification(),
                String.valueOf(response.entryTime()),
                String.valueOf(response.startTime()),
                String.valueOf(response.dischargeTime())
        );

    }
}
