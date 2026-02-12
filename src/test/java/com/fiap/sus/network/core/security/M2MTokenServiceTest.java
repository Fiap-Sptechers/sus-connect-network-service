package com.fiap.sus.network.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class M2MTokenServiceTest {

    private M2MTokenService tokenService;
    private String validPrivateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        tokenService = new M2MTokenService();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        String base64PrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        validPrivateKey = "-----BEGIN PRIVATE KEY-----\n" + base64PrivateKey + "\n-----END PRIVATE KEY-----";

        ReflectionTestUtils.setField(tokenService, "privateKeyContent", validPrivateKey);
        ReflectionTestUtils.setField(tokenService, "issuer", "test-issuer");
        ReflectionTestUtils.setField(tokenService, "liveopsAudience", "test-audience");
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = tokenService.generateToken();

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_ShouldContainCorrectIssuer() {
        String token = tokenService.generateToken();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("test-issuer", claims.getIssuer());
    }

    @Test
    void generateToken_ShouldContainCorrectAudience() {
        String token = tokenService.generateToken();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("test-audience", claims.getAudience());
    }

    @Test
    void generateToken_ShouldContainIssuedAtTimestamp() {
        String token = tokenService.generateToken();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getIssuedAt());
        assertTrue(claims.getIssuedAt().before(new Date()) || claims.getIssuedAt().equals(new Date()));
    }

    @Test
    void generateToken_ShouldContainExpirationTimestamp() {
        String token = tokenService.generateToken();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateToken_ShouldExpireInFiveMinutes() {
        String token = tokenService.generateToken();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        long issuedAt = claims.getIssuedAt().getTime();
        long expirationTime = claims.getExpiration().getTime();
        long fiveMinutesInMillis = 5 * 60 * 1000;
        long actualDuration = expirationTime - issuedAt;

        assertTrue(actualDuration >= fiveMinutesInMillis - 2000);
        assertTrue(actualDuration <= fiveMinutesInMillis + 2000);
    }

    @Test
    void generateToken_ShouldContainUniqueJwtId() {
        String token1 = tokenService.generateToken();
        String token2 = tokenService.generateToken();

        Claims claims1 = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token1)
                .getBody();

        Claims claims2 = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token2)
                .getBody();

        assertNotNull(claims1.getId());
        assertNotNull(claims2.getId());
        assertNotEquals(claims1.getId(), claims2.getId());
    }

    @Test
    void generateToken_ShouldBeSignedWithRS256Algorithm() {
        String token = tokenService.generateToken();

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        String header = new String(Base64.getUrlDecoder().decode(parts[0]));
        assertTrue(header.contains("RS256"));
    }

    @Test
    void generateToken_ShouldThrowSecurityException_WhenPrivateKeyIsInvalid() {
        ReflectionTestUtils.setField(tokenService, "privateKeyContent", "invalid-key");

        SecurityException exception = assertThrows(SecurityException.class,
                () -> tokenService.generateToken());

        assertTrue(exception.getMessage().contains("Failed to generate M2M token"));
    }

    @Test
    void generateToken_ShouldThrowSecurityException_WhenPrivateKeyIsMalformed() {
        String malformedKey = "-----BEGIN PRIVATE KEY-----\nInvalidBase64Content!!!\n-----END PRIVATE KEY-----";
        ReflectionTestUtils.setField(tokenService, "privateKeyContent", malformedKey);

        SecurityException exception = assertThrows(SecurityException.class,
                () -> tokenService.generateToken());

        assertTrue(exception.getMessage().contains("Failed to generate M2M token"));
    }

    @Test
    void generateToken_ShouldThrowSecurityException_WhenPrivateKeyIsEmpty() {
        ReflectionTestUtils.setField(tokenService, "privateKeyContent", "");

        SecurityException exception = assertThrows(SecurityException.class,
                () -> tokenService.generateToken());

        assertTrue(exception.getMessage().contains("Failed to generate M2M token"));
    }

    @Test
    void generateToken_ShouldHandlePrivateKeyWithoutHeaders() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String base64PrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        ReflectionTestUtils.setField(tokenService, "privateKeyContent", base64PrivateKey);

        String token = tokenService.generateToken();

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_ShouldHandlePrivateKeyWithWhitespace() {
        String keyWithWhitespace = validPrivateKey.replace("\n", "\n   \t  ");
        ReflectionTestUtils.setField(tokenService, "privateKeyContent", keyWithWhitespace);

        String token = tokenService.generateToken();

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_ShouldCreateDifferentTokensOnSubsequentCalls() {
        String token1 = tokenService.generateToken();
        String token2 = tokenService.generateToken();

        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_ShouldCreateValidTokenThatCanBeVerified() {
        String token = tokenService.generateToken();

        assertDoesNotThrow(() -> {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
        });
    }
}

