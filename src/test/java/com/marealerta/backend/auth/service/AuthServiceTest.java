package com.marealerta.backend.auth.service;

import com.marealerta.backend.auth.dto.LoginRequest;
import com.marealerta.backend.auth.dto.RefreshTokenRequest;
import com.marealerta.backend.auth.dto.RegisterRequest;
import com.marealerta.backend.auth.dto.TokenResponse;
import com.marealerta.backend.checklist.service.ChecklistService;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoRequest;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.usuario.model.Usuario;
import com.marealerta.backend.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ChecklistService checklistService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private Estabelecimento estabelecimento;

    @BeforeEach
    void setUp() {
        estabelecimento = Estabelecimento.builder()
                .id(1L)
                .nome("Mercado Central")
                .documento("12345678000199")
                .tipoNegocio(TipoNegocio.MERCADO)
                .bairro(Bairro.MARCO)
                .ativo(true)
                .build();

        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("test@marealerta.com");
        usuario.setSenha("encoded-pass");
        usuario.setEstabelecimento(estabelecimento);
    }

    @Test
    @DisplayName("Deve registrar novo usuário e estabelecimento com sucesso")
    void deveRegistrarUsuarioComSucesso() {
        EstabelecimentoRequest estabDto = EstabelecimentoRequest.builder()
                .nome("Mercado Central")
                .documento("12345678000199")
                .tipoNegocio(TipoNegocio.MERCADO)
                .bairro(Bairro.MARCO)
                .responsavel("Carlos")
                .logradouro("Rua A")
                .numero("123")
                .build();

        RegisterRequest request = RegisterRequest.builder()
                .email("novo@marealerta.com")
                .senha("senha12345")
                .estabelecimento(estabDto)
                .build();

        when(usuarioRepository.existsByEmail("novo@marealerta.com")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("encoded-senha");
        when(jwtService.gerarToken(any(Usuario.class))).thenReturn("access-token-123");
        when(jwtService.gerarRefreshToken(any(Usuario.class))).thenReturn("refresh-token-123");
        when(jwtService.getTempoExp()).thenReturn(3600000L);

        TokenResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.getEmail()).isEqualTo("novo@marealerta.com");
        verify(estabelecimentoRepository).save(any(Estabelecimento.class));
        verify(checklistService).gerarItensPadraoParaEstabelecimento(any(Estabelecimento.class));
        verify(usuarioRepository).saveAndFlush(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar e-mail já cadastrado")
    void deveLancarExcecaoAoRegistrarEmailDuplicado() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@marealerta.com")
                .senha("senha12345")
                .build();

        when(usuarioRepository.existsByEmail("test@marealerta.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    @DisplayName("Deve autenticar com credenciais válidas e retornar tokens")
    void deveFazerLoginComSucesso() {
        LoginRequest request = new LoginRequest("test@marealerta.com", "senha12345");
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(usuario, "senha12345");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authToken);
        when(jwtService.gerarToken(usuario)).thenReturn("access-token-123");
        when(jwtService.gerarRefreshToken(usuario)).thenReturn("refresh-token-123");
        when(jwtService.getTempoExp()).thenReturn(3600000L);

        TokenResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.getEstabelecimentoId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar InvalidCredentialsException quando autenticação falhar")
    void deveLancarExcecaoAoFalharLogin() {
        LoginRequest request = new LoginRequest("test@marealerta.com", "senha-errada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Deve renovar token através de refresh token válido")
    void deveRenovarTokenComSucesso() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        when(jwtService.isRefreshTokenValido("valid-refresh-token")).thenReturn(true);
        when(jwtService.getUserName("valid-refresh-token")).thenReturn("test@marealerta.com");
        when(usuarioRepository.findByEmail("test@marealerta.com")).thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(usuario)).thenReturn("new-access-token");
        when(jwtService.gerarRefreshToken(usuario)).thenReturn("new-refresh-token");
        when(jwtService.getTempoExp()).thenReturn(3600000L);

        TokenResponse response = authService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("Deve recusar renovação com refresh token inválido")
    void deveRecusarRenovacaoComTokenInvalido() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

        when(jwtService.isRefreshTokenValido("invalid-refresh-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
