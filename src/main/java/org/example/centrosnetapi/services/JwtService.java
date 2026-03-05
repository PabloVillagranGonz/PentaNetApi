package org.example.centrosnetapi.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.example.centrosnetapi.models.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // ============================================================
    // KEY
    // ============================================================

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ============================================================
    // GENERAR TOKEN
    // ============================================================

    public String generateToken(Usuario usuario) {

        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ============================================================
    // EXTRAER EMAIL
    // ============================================================

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // ============================================================
    // VALIDAR TOKEN
    // ============================================================

    public boolean isTokenValid(String token, Usuario usuario) {

        try {
            final String email = extractEmail(token);

            return email.equals(usuario.getEmail())
                    && !isTokenExpired(token)
                    && Boolean.TRUE.equals(usuario.getActivo());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ============================================================
    // CLAIMS
    // ============================================================

    private Claims extractClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }
}