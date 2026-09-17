package com.marealerta.backend.auth.service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;import org.springframework.security.core.Authentication;import org.springframework.security.core.userdetails.UserDetails;import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.expiration}")
    private long tempoExp;

    @Value("${jwt.key}")
    private String chave;


    //gerar token
    public String gerarT(Authentication authentication){
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return buildT(user.getUsername());
    }

    private String buildT(String username){
        Date now = new Date();
        Date exp = new Date(now.getTime() + tempoExp);

        return  Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getAssinaturaChave())
                .compact();
    }

    private SecretKey getAssinaturaChave(){
        return Keys.hmacShaKeyFor(chave.getBytes());
    }

    public boolean isTokenValido(String token){
        try{
         getClaims(token);
         return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    public String getUserName(String token){
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token){
        //validar assinatura
        //validar expiracao
        return Jwts.parser()
                .verifyWith(getAssinaturaChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
