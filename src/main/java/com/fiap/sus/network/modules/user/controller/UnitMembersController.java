package com.fiap.sus.network.modules.user.controller;

import com.fiap.sus.network.modules.user.dto.MemberRequest;
import com.fiap.sus.network.modules.user.dto.UserResponse;
import com.fiap.sus.network.modules.user.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import com.fiap.sus.network.modules.health_unit.repository.HealthUnitRepository;
import com.fiap.sus.network.modules.user.mapper.UserMapper;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/units/{unitId}/users")
@RequiredArgsConstructor
public class UnitMembersController {

    private final AccessControlService accessControlService;
    private final UserRepository userRepository;
    private final HealthUnitRepository unitSaudeRepository;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<List<UserResponse>> listMembers(@PathVariable UUID unitId) {
        return ResponseEntity.ok(accessControlService.listMembers(unitId, userMapper));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<Void> addMember(@PathVariable UUID unitId, @RequestBody MemberRequest request) {
        accessControlService.addMember(unitId, request, unitSaudeRepository, userRepository);
        return ResponseEntity.ok().build();
    }
}
