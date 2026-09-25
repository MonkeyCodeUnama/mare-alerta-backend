package com.marealerta.backend.auth.service;
import com.marealerta.backend.auth.dto.LoginRequest;
import com.marealerta.backend.auth.dto.RegisterRequest;
import com.marealerta.backend.auth.dto.TokenResponse;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.usuario.dto.UsuarioResponse;
import com.marealerta.backend.usuario.model.Usuario;
import com.marealerta.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public TokenResponse register(RegisterRequest register) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByEmail(register.getEmail())
                .orElse(null);

        if(usuario != null){
            throw new BadRequestException("usuario ja cadastrado");
        }

        Estabelecimento estab = new Estabelecimento();
        estab.setNome(register.getEstabelecimento().getNome());
        estab.setDocumento(register.getEstabelecimento().getDocumento());
        estab.setTipoNegocio(register.getEstabelecimento().getTipoNegocio());
        estab.setLatitude(register.getEstabelecimento().getLatitude());
        estab.setLongitude(register.getEstabelecimento().getLongitude());
        estab.setEndereco(register.getEstabelecimento().getEndereco());

        estabelecimentoRepository.save(estab);

        Usuario user = new Usuario();
        user.setEmail(register.getEmail());
        user.setSenha(passwordEncoder.encode(register.getSenha()));
        user.setEstabelecimento(estab);

        usuarioRepository.save(user);

        String token = jwtService.gerarT(user);
        return new TokenResponse(token);

    }

    public TokenResponse login(LoginRequest request) throws BadRequestException {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()
                    )
            );

            Usuario user = (Usuario) auth.getPrincipal();

            String token = jwtService.gerarT(user);

            return new TokenResponse(token);

        } catch (BadCredentialsException e) {
            throw new BadRequestException("credenciais invalidas");
        } catch (Exception e){
            throw e;
        }
    }
}
