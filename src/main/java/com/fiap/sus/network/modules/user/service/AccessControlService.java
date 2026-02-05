package com.fiap.sus.network.modules.user.service;

import com.fiap.sus.network.modules.user.entity.Role;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import com.fiap.sus.network.modules.user.entity.User;
import com.fiap.sus.network.modules.user.entity.UserUnit;
import com.fiap.sus.network.modules.user.repository.RoleRepository;
import com.fiap.sus.network.modules.user.repository.UserUnitRepository;
import com.fiap.sus.network.core.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import com.fiap.sus.network.modules.user.dto.MemberRequest;
import com.fiap.sus.network.modules.user.dto.UserResponse;
import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.modules.user.mapper.UserMapper;
import com.fiap.sus.network.modules.health_unit.repository.HealthUnitRepository;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final UserUnitRepository userUnidadeRepository;
    private final RoleRepository roleRepository;

    public void checkAccess(UUID unitId) {
        if (isAdmin()) return;

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        boolean hasAccess = userUnidadeRepository.existsByUserIdAndUnitId(userDetails.getId(), unitId);
        
        if (!hasAccess) {
            throw new AccessDeniedException("User does not have access to this Unit.");
        }
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }
    
    @Transactional
    public void grantUnitAccess(User currentUser, User targetUser, HealthUnit unit, String roleName) {
        Role targetRole = roleRepository.findByName(roleName).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        
        if (!isAdmin()) {
            UserUnit currentMemberRole = userUnidadeRepository.findByUserIdAndUnitId(currentUser.getId(), unit.getId())
                    .orElseThrow(() -> new AccessDeniedException("You are not a member of this unit"));
            
            if (targetRole.getLevel() > currentMemberRole.getRole().getLevel()) {
                 throw new AccessDeniedException("Cannot assign a role higher than your own.");
            }
        }
        
        UserUnit assignment = new UserUnit();
        assignment.setUser(targetUser);
        assignment.setUnit(unit);
        assignment.setRole(targetRole);
        
        userUnidadeRepository.save(assignment);
    }

    // System internal use (auto-assign)
    @Transactional
    public void grantUnitAccessSystem(User targetUser, HealthUnit unit, String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        UserUnit assignment = new UserUnit();
        assignment.setUser(targetUser);
        assignment.setUnit(unit);
        assignment.setRole(role);
        userUnidadeRepository.save(assignment);
    }

    public List<UserResponse> listMembers(UUID unitId, UserMapper userMapper) {
        checkAccess(unitId);
        return userUnidadeRepository.findByUnitId(unitId).stream()
                .map(uu -> uu.getUser())
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    public void addMember(UUID unitId, MemberRequest request, HealthUnitRepository unitRepository, UserRepository userRepository) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId()).orElseThrow();
        
        User targetUser = userRepository.findById(request.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        HealthUnit unit = unitRepository.findById(unitId).orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
        
        grantUnitAccess(currentUser, targetUser, unit, request.roleName());
    }
}
