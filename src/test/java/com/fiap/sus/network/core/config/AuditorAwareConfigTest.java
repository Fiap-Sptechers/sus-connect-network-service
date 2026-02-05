package com.fiap.sus.network.core.config;

import com.fiap.sus.network.core.security.CustomUserDetails;
import com.fiap.sus.network.modules.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditorAwareConfigTest {

    private AuditorAwareConfig auditorAwareConfig;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        auditorAwareConfig = new AuditorAwareConfig();
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    void getCurrentAuditor_ShouldReturnEmpty_WhenNoAuth() {
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mock(SecurityContext.class));
        Optional<UUID> result = auditorAwareConfig.getCurrentAuditor();
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentAuditor_ShouldReturnUserId_WhenAuthenticated() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        Optional<UUID> result = auditorAwareConfig.getCurrentAuditor();
        
        assertTrue(result.isPresent());
        assertEquals(userId, result.get());
    }
}
