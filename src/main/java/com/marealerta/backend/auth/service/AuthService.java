package com.marealerta.backend.auth.service;
import com.marealerta.backend.auth.dto.RegisterRequest;
import com.marealerta.backend.auth.dto.TokenResponse;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.usuario.dto.UsuarioResponse;
import com.marealerta.backend.usuario.model.Usuario;
import com.marealerta.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public UsuarioResponse register(RegisterRequest register) throws BadRequestException {
        Usuario usuario = usuarioRepository.findByEmail(register.getEmail())
                .orElse(null);

        if(usuario != null){
            throw new BadRequestException("email ja cadastrado");
        }



        Estabelecimento estab = new Estabelecimento();
        estab.setNome(register.getEstabelecimentoRequest().getNome());
        estab.setDocumento(register.getEstabelecimentoRequest().getDocumento());
        estab.setTipoNegocio(register.getEstabelecimentoRequest().getTipoNegocio());
        estab.setLatitude(register.getEstabelecimentoRequest().getLatitude());
        estab.setLongitude(register.getEstabelecimentoRequest().getLongitude());
        estab.setEndereco(register.getEstabelecimentoRequest().getEndereco());

        estabelecimentoRepository.save(estab);

        Usuario user = new Usuario();
        user.setEmail(register.getEmail());
        user.setSenha(passwordEncoder.encode(register.getSenha()));
        user.setEstabelecimento(estab);

        usuarioRepository.save(user);

        return new UsuarioResponse(
                user.getId(),
                user.getEmail(),
                estab.getNome()
        );

    }

    public void login(){

    }
}
