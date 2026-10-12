package com.marealerta.backend.auth.service;

import com.marealerta.backend.usuario.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "tempoExp", 3600000L);
        ReflectionTestUtils.setField(jwtService, "tempoRefreshExp", 86400000L);
        ReflectionTestUtils.setField(jwtService, "chave", "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=");

        usuario = new Usuario();
        usuario.setEmail("test@marealerta.com");
    }

    @Test
    @DisplayName("Deve gerar e validar token de acesso com sucesso")
    void deveGerarEValidarTokenDeAcesso() {
        String token = jwtService.gerarToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValido(token)).isTrue();
        assertThat(jwtService.isRefreshTokenValido(token)).isFalse();
        assertThat(jwtService.getUserName(token)).isEqualTo("test@marealerta.com");
    }

    @Test
    @DisplayName("Deve gerar e validar refresh token com sucesso")
    void deveGerarEValidarRefreshToken() {
        String refreshToken = jwtService.gerarRefreshToken(usuario);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.isRefreshTokenValido(refreshToken)).isTrue();
        assertThat(jwtService.isTokenValido(refreshToken)).isFalse();
        assertThat(jwtService.getUserName(refreshToken)).isEqualTo("test@marealerta.com");
    }

    @Test
    @DisplayName("Deve rejeitar token adulterado ou inválido")
    void deveRejeitarTokenInvalido() {
        assertThat(jwtService.isTokenValido("token-invalido-123")).isFalse();
        assertThat(jwtService.isRefreshTokenValido("token-invalido-123")).isFalse();
    }
}
