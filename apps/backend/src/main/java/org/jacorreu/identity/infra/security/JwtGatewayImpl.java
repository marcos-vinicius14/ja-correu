package org.jacorreu.identity.infra.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.jacorreu.identity.core.gateway.JwtGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtGatewayImpl implements JwtGateway {

    private final SecretKey key;

    public JwtGatewayImpl(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    public String generateToken(UUID userId, String email, String username) {
        return  Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration((new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)))
                .claim("user_id", userId.toString())
                .claim("username", username)
                .signWith(key)
                .compact();
    }

    @Override
    public UUID extractUserId(String token) {
        String userId =  Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("user_id", String.class);

        return UUID.fromString(userId);
    }
}
