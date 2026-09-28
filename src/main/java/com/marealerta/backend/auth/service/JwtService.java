package com.marealerta.backend.auth.service;
import com.marealerta.backend.usuario.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;import org.springframework.security.core.Authentication;import org.springframework.security.core.userdetails.UserDetails;import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.expiration}")
    private long tempoExp;

    @Value("${jwt.key}")
    private String chave;


    public String gerarT(Usuario usuario){
        return buildT(usuario.getUsername());
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
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(chave));
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
        return Jwts.parser()
                .verifyWith(getAssinaturaChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
