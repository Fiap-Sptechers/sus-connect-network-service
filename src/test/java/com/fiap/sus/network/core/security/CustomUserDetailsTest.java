package com.fiap.sus.network.core.security;

import com.fiap.sus.network.modules.user.entity.Role;
import com.fiap.sus.network.modules.user.entity.User;
import com.fiap.sus.network.modules.user.entity.UserUnit;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void getAuthorities_ShouldIncludeGlobalAndUnitRoles() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCpfCnpj("123");
        user.setPassword("pass");

        Role globalRole = new Role();
        globalRole.setName("ADMIN");
        user.setGlobalRoles(Set.of(globalRole));

        UserUnit userUnit = new UserUnit();
        Role unitRole = new Role();
        unitRole.setName("MANAGER");
        userUnit.setRole(unitRole);
        HealthUnit unit = new HealthUnit();
        unit.setId(UUID.randomUUID());
        userUnit.setUnit(unit);
        user.setUnitRoles(Set.of(userUnit));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("MANAGER")));
        assertEquals("pass", userDetails.getPassword());
        assertEquals("123", userDetails.getUsername());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }
}
