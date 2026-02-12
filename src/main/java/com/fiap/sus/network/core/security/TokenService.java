package com.fiap.sus.network.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fiap.sus.network.modules.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("SusConnect-Network")
                    .withSubject(user.getCpfCnpj())
                    .withClaim("name", user.getName())
                    .withClaim("id", user.getId().toString())
                    .withClaim("roles", buildRolesClaim(user))
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("SusConnect-Network")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    private List<Map<String, Object>> buildRolesClaim(User user) {
        List<Map<String, Object>> roles = new ArrayList<>();
        
        // Global roles - Defensive copy to avoid ConcurrentModificationException with Hibernate Sets
        if (user.getGlobalRoles() != null) {
            new ArrayList<>(user.getGlobalRoles()).forEach(role -> {
                roles.add(Map.of("role", role.getName(), "unitId", "GLOBAL", "level", role.getLevel()));
            });
        }
        
        // Unit roles - Defensive copy
        if (user.getUnitRoles() != null) {
            new ArrayList<>(user.getUnitRoles()).forEach(ur -> {
                if (ur.getRole() != null && ur.getUnit() != null) {
                    roles.add(Map.of("role", ur.getRole().getName(), "unitId", ur.getUnit().getId().toString(), "level", ur.getRole().getLevel()));
                }
            });
        }
        
        return roles;
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusNanos(expiration * 1000000).toInstant(ZoneOffset.of("-03:00"));
    }
}
