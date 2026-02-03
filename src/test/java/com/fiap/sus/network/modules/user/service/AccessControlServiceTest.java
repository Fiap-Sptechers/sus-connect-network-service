package com.fiap.sus.network.modules.user.service;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.core.security.CustomUserDetails;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import com.fiap.sus.network.modules.health_unit.repository.HealthUnitRepository;
import com.fiap.sus.network.modules.user.dto.MemberRequest;
import com.fiap.sus.network.modules.user.dto.UserResponse;
import com.fiap.sus.network.modules.user.entity.Role;
import com.fiap.sus.network.modules.user.entity.User;
import com.fiap.sus.network.modules.user.entity.UserUnit;
import com.fiap.sus.network.modules.user.mapper.UserMapper;
import com.fiap.sus.network.modules.user.repository.RoleRepository;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import com.fiap.sus.network.modules.user.repository.UserUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private UserUnitRepository userUnitRepository;
    @Mock
    private RoleRepository roleRepository;
    
    @InjectMocks
    private AccessControlService service;

    private UUID userId;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setCpfCnpj("admin");
        userDetails = new CustomUserDetails(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(authentication.getAuthorities()).thenReturn((Collection) Collections.emptyList());
        
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void checkAccess_ShouldDoNothing_WhenAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ADMIN")));
        
        assertDoesNotThrow(() -> service.checkAccess(UUID.randomUUID()));
    }

    @Test
    void checkAccess_ShouldThrow_WhenNoAccess() {
        UUID unitId = UUID.randomUUID();
        when(userUnitRepository.existsByUserIdAndUnitId(userId, unitId)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> service.checkAccess(unitId));
    }
    
    @Test
    void grantUnitAccess_ShouldGrant_WhenAllowed() {
        User currentUser = new User();
        currentUser.setId(userId);
        User targetUser = new User();
        HealthUnit unit = new HealthUnit();
        unit.setId(UUID.randomUUID());
        String roleName = "OPERATOR";
        
        Role targetRole = new Role();
        targetRole.setName("OPERATOR");
        targetRole.setLevel(10);
        
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setLevel(100);
        
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(targetRole));
        
        // Mock Admin check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ADMIN")));
        
        service.grantUnitAccess(currentUser, targetUser, unit, roleName);
        
        verify(userUnitRepository).save(any(UserUnit.class));
    }

    @Test
    void addMember_ShouldCallGrantAccess() {
        UUID unitId = UUID.randomUUID();
        MemberRequest request = new MemberRequest(UUID.randomUUID(), "OPERATOR");
        HealthUnitRepository huRepo = mock(HealthUnitRepository.class);
        UserRepository uRepo = mock(UserRepository.class);
        
        when(uRepo.findById(userId)).thenReturn(Optional.of(new User()));
        when(uRepo.findById(request.userId())).thenReturn(Optional.of(new User()));
        when(huRepo.findById(unitId)).thenReturn(Optional.of(new HealthUnit()));
        when(roleRepository.findByName("OPERATOR")).thenReturn(Optional.of(new Role()));
        
        // Mock Admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ADMIN")));
        
        service.addMember(unitId, request, huRepo, uRepo);
        
        verify(userUnitRepository).save(any(UserUnit.class));
    }
}
