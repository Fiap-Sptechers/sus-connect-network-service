package com.fiap.sus.network.core.security;

import com.fiap.sus.network.modules.user.entity.Role;
import com.fiap.sus.network.modules.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String name;
    private final String cpfCnpj;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.cpfCnpj = user.getCpfCnpj();
        this.password = user.getPassword();
        
        // Combine Global Roles and Unit Roles for Authority Check (Optional: Namespace Unit Roles like UNIT_1_ADMIN)
        // For simple RBAC, we might just put "ADMIN" if global, and maybe generic unit roles if we want check.
        // But the requirement says "Policy ... he can only interact with resources related to his own health units".
        // This is Authorization Logic, not just Authentication.
        this.authorities = Stream.concat(
            user.getGlobalRoles() != null ? new ArrayList<>(user.getGlobalRoles()).stream().map(Role::getName) : Stream.empty(),
            user.getUnitRoles() != null ? new ArrayList<>(user.getUnitRoles()).stream().map(ur -> ur.getRole().getName()) : Stream.empty()
        ).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return !false; } // deleted check handled in query usually


    public UUID getId() { return id; }
    


    public String getName() { return name; }
    public String getUsername() { return cpfCnpj; }
    public String getPassword() { return password; }
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    
}
