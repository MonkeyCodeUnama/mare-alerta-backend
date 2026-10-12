package com.marealerta.backend.auth.service;

import com.marealerta.backend.usuario.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    @Value("${jwt.expiration:9000000}")
    private long tempoExp;

    @Value("${jwt.refresh-expiration:604800000}")
    private long tempoRefreshExp;

    @Value("${jwt.key:MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=}")
    private String chave;

    public String gerarT(Usuario usuario) {
        return gerarToken(usuario);
    }

    public String gerarToken(Usuario usuario) {
        return buildToken(usuario.getUsername(), Map.of(CLAIM_TYPE, TYPE_ACCESS), tempoExp);
    }

    public String gerarRefreshToken(Usuario usuario) {
        return buildToken(usuario.getUsername(), Map.of(CLAIM_TYPE, TYPE_REFRESH), tempoRefreshExp);
    }

    private String buildToken(String username, Map<String, Object> claims, long expirationMillis) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getAssinaturaChave())
                .compact();
    }

    private SecretKey getAssinaturaChave() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(chave));
    }

    public boolean isTokenValido(String token) {
        try {
            Claims claims = getClaims(token);
            Object type = claims.get(CLAIM_TYPE);
            return type == null || TYPE_ACCESS.equals(type);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshTokenValido(String token) {
        try {
            Claims claims = getClaims(token);
            Object type = claims.get(CLAIM_TYPE);
            return TYPE_REFRESH.equals(type);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUserName(String token) {
        return getClaims(token).getSubject();
    }

    public long getTempoExp() {
        return tempoExp;
    }

    public long getTempoRefreshExp() {
        return tempoRefreshExp;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getAssinaturaChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
