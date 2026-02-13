package com.fiap.sus.network.core.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class M2MTokenService {

    @Value("${jwt.liveops.private-key}")
    private String privateKeyContent;

    @Value("${jwt.liveops.issuer}")
    private String issuer;

    @Value("${jwt.liveops.audience}")
    private String liveopsAudience;

    public String generateToken() {
        try {
            PrivateKey privateKey = loadPrivateKey(privateKeyContent);

            return Jwts.builder()
                    .setIssuer(issuer)
                    .setAudience(liveopsAudience)
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)))
                    .setId(UUID.randomUUID().toString())
                    .signWith(privateKey, SignatureAlgorithm.RS256)
                    .compact();

        } catch (Exception e) {
            throw new SecurityException("Failed to generate M2M token: " + e.getMessage());
        }
    }

    private PrivateKey loadPrivateKey(String keyContent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String cleanKey = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "");

        String sanitizedKey = cleanKey
                .replace("\\n", "")
                .replace("\\r", "")
                .replace("\"", "")
                .replace("'", "")
                .replaceAll("\\s+", "")
                .replace("\\", "");

        byte[] keyBytes = Base64.getDecoder().decode(sanitizedKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

}
