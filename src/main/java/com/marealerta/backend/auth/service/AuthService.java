package com.marealerta.backend.auth.service;

import com.marealerta.backend.auth.dto.LoginRequest;
import com.marealerta.backend.auth.dto.RefreshTokenRequest;
import com.marealerta.backend.auth.dto.RegisterRequest;
import com.marealerta.backend.auth.dto.TokenResponse;
import com.marealerta.backend.checklist.service.ChecklistService;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.usuario.model.Usuario;
import com.marealerta.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ChecklistService checklistService;

    @Transactional
    public TokenResponse register(RegisterRequest register) {
        String emailNormalizado = register.getEmail() != null ? register.getEmail().trim().toLowerCase() : "";
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new EmailAlreadyRegisteredException();
        }

        var dto = register.getEstabelecimento();
        Estabelecimento estab = Estabelecimento.builder()
                .nome(dto.getNome())
                .documento(dto.getDocumento())
                .tipoNegocio(dto.getTipoNegocio() != null ? dto.getTipoNegocio() : TipoNegocio.OUTROS)
                .responsavel(dto.getResponsavel() != null && !dto.getResponsavel().isBlank() ? dto.getResponsavel() : "Não informado")
                .bairro(dto.getBairro() != null ? dto.getBairro() : Bairro.MARCO)
                .logradouro(dto.getLogradouro() != null && !dto.getLogradouro().isBlank() ? dto.getLogradouro() : "Não informado")
                .numero(dto.getNumero() != null && !dto.getNumero().isBlank() ? dto.getNumero() : "S/N")
                .complemento(dto.getComplemento())
                .cep(dto.getCep())
                .municipio(dto.getMunicipio() != null && !dto.getMunicipio().isBlank() ? dto.getMunicipio() : "Belém")
                .uf(dto.getUf() != null && !dto.getUf().isBlank() ? dto.getUf() : "PA")
                .telefone(dto.getTelefone())
                .ativo(true)
                .build();

        estabelecimentoRepository.save(estab);

        checklistService.gerarItensPadraoParaEstabelecimento(estab);

        Usuario user = new Usuario();
        user.setEmail(emailNormalizado);
        user.setSenha(passwordEncoder.encode(register.getSenha()));
        user.setEstabelecimento(estab);

        usuarioRepository.saveAndFlush(user);

        String token = jwtService.gerarToken(user);
        String refreshToken = jwtService.gerarRefreshToken(user);

        return TokenResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("Bearer")
                .expiraEm(jwtService.getTempoExp() / 1000)
                .estabelecimentoId(estab.getId())
                .email(user.getEmail())
                .build();
    }

    public TokenResponse login(LoginRequest request) {
        try {
            String emailNormalizado = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            emailNormalizado,
                            request.getSenha()
                    )
            );

            Usuario user = (Usuario) auth.getPrincipal();

            if (user.getDeletedAt() != null || (user.getEstabelecimento() != null && Boolean.FALSE.equals(user.getEstabelecimento().getAtivo()))) {
                throw new InvalidCredentialsException();
            }

            String token = jwtService.gerarToken(user);
            String refreshToken = jwtService.gerarRefreshToken(user);
            Long estabId = user.getEstabelecimento() != null ? user.getEstabelecimento().getId() : null;

            return TokenResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("Bearer")
                .expiraEm(jwtService.getTempoExp() / 1000)
                .estabelecimentoId(estabId)
                .email(user.getEmail())
                .build();

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtService.isRefreshTokenValido(token)) {
            throw new InvalidCredentialsException();
        }

        String email = jwtService.getUserName(token);
        Usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getDeletedAt() != null || (user.getEstabelecimento() != null && Boolean.FALSE.equals(user.getEstabelecimento().getAtivo()))) {
            throw new InvalidCredentialsException();
        }

        String novoAccessToken = jwtService.gerarToken(user);
        String novoRefreshToken = jwtService.gerarRefreshToken(user);
        Long estabId = user.getEstabelecimento() != null ? user.getEstabelecimento().getId() : null;

        return TokenResponse.builder()
                .token(novoAccessToken)
                .refreshToken(novoRefreshToken)
                .tipo("Bearer")
                .expiraEm(jwtService.getTempoExp() / 1000)
                .estabelecimentoId(estabId)
                .email(user.getEmail())
                .build();
    }
}
