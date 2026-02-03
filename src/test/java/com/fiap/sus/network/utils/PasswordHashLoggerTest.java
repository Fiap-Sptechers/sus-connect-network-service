package com.fiap.sus.network.utils;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashLoggerTest {

    @Test
    void logAdminPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin";
        String hash = encoder.encode(password);
        
        System.out.println("\n========================================");
        System.out.println("ADMIN PASSWORD HASH GENERATOR");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("========================================\n");
    }
}
