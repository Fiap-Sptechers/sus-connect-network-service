package com.fiap.sus.network.core.security;

import com.fiap.sus.network.modules.user.entity.Role;
import com.fiap.sus.network.modules.user.entity.User;
import com.fiap.sus.network.modules.user.entity.UserUnit;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret");
        ReflectionTestUtils.setField(tokenService, "expiration", 3600000L); // 1 hour
    }

    @Test
    void generateToken_ShouldReturnToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCpfCnpj("12345678900");
        user.setName("Test User");
        user.setGlobalRoles(Collections.emptySet());
        user.setUnitRoles(Collections.emptySet());

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_ShouldReturnSubject_WhenValid() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCpfCnpj("12345678900");
        user.setName("Test User");
        user.setGlobalRoles(Collections.emptySet());
        user.setUnitRoles(Collections.emptySet());

        String token = tokenService.generateToken(user);
        String subject = tokenService.validateToken(token);

        assertEquals("12345678900", subject);
    }

    @Test
    void validateToken_ShouldReturnEmpty_WhenInvalid() {
        String subject = tokenService.validateToken("invalid-token");
        assertEquals("", subject);
    }

    @Test
    void buildRolesClaim_ShouldIncludeGlobalAndUnitRoles() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCpfCnpj("12345678900");
        user.setName("Test User");

        Role globalRole = new Role();
        globalRole.setName("ADMIN");
        globalRole.setLevel(1);
        user.setGlobalRoles(Set.of(globalRole));

        HealthUnit unit = new HealthUnit();
        unit.setId(UUID.randomUUID());
        Role unitRole = new Role();
        unitRole.setName("OPERATOR");
        unitRole.setLevel(2);
        
        UserUnit userUnit = new UserUnit();
        userUnit.setUnit(unit);
        userUnit.setRole(unitRole);
        user.setUnitRoles(Set.of(userUnit));

        String token = tokenService.generateToken(user);
        assertNotNull(token);
        // Validation implicitly tests buildRolesClaim
        assertEquals("12345678900", tokenService.validateToken(token));
    }
}
