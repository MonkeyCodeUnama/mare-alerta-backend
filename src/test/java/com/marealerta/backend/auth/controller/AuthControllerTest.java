package com.marealerta.backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marealerta.backend.auth.dto.LoginRequest;
import com.marealerta.backend.auth.dto.RefreshTokenRequest;
import com.marealerta.backend.auth.dto.RegisterRequest;
import com.marealerta.backend.auth.dto.TokenResponse;
import com.marealerta.backend.auth.service.AuthService;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoRequest;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import com.marealerta.backend.estabelecimento.service.EstabelecimentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private EstabelecimentoService estabelecimentoService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("Deve registrar usuário via POST /auth/registro com status 201")
    void deveRegistrarUsuario() throws Exception {
        EstabelecimentoRequest estab = EstabelecimentoRequest.builder()
                .nome("Padaria Silva")
                .documento("12345678000100")
                .tipoNegocio(TipoNegocio.MERCADO)
                .bairro(Bairro.MARCO)
                .build();

        RegisterRequest request = RegisterRequest.builder()
                .email("padaria@marealerta.com")
                .senha("senhaForte123")
                .estabelecimento(estab)
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .token("jwt-token")
                .refreshToken("refresh-token")
                .tipo("Bearer")
                .expiraEm(3600L)
                .email("padaria@marealerta.com")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.email").value("padaria@marealerta.com"));
    }

    @Test
    @DisplayName("Deve realizar login via POST /auth/login com status 200")
    void deveRealizarLogin() throws Exception {
        LoginRequest request = new LoginRequest("padaria@marealerta.com", "senhaForte123");

        TokenResponse tokenResponse = TokenResponse.builder()
                .token("jwt-token")
                .refreshToken("refresh-token")
                .tipo("Bearer")
                .expiraEm(3600L)
                .email("padaria@marealerta.com")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("Deve renovar token via POST /auth/refresh com status 200")
    void deveRenovarToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        TokenResponse tokenResponse = TokenResponse.builder()
                .token("new-jwt-token")
                .refreshToken("new-refresh-token")
                .tipo("Bearer")
                .expiraEm(3600L)
                .email("padaria@marealerta.com")
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("Deve retornar dados do estabelecimento via GET /auth/me quando autenticado")
    void deveRetornarDadosUsuarioAutenticado() throws Exception {
        EstabelecimentoResponse estabResponse = EstabelecimentoResponse.builder()
                .id(1L)
                .nome("Padaria Silva")
                .documento("12345678000100")
                .tipoNegocio(TipoNegocio.MERCADO)
                .bairro(Bairro.MARCO)
                .build();

        Principal mockPrincipal = () -> "padaria@marealerta.com";
        when(estabelecimentoService.buscarPorEmailUsuario("padaria@marealerta.com")).thenReturn(estabResponse);

        mockMvc.perform(get("/auth/me").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Padaria Silva"))
                .andExpect(jsonPath("$.documento").value("12345678000100"));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized via GET /auth/me quando principal for nulo")
    void deveRetornar401EmMeSemPrincipal() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
