package com.marealerta.backend.shared.config;

import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.usuario.model.Role;
import com.marealerta.backend.usuario.model.Usuario;
import com.marealerta.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.seed-enabled:false}")
    private boolean seedEnabled;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled || adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        String emailNormalizado = adminEmail.trim().toLowerCase();
        var existente = usuarioRepository.findByEmail(emailNormalizado);
        if (existente.isPresent()) {
            Usuario user = existente.get();
            if (user.getRole() != Role.ADMIN) {
                user.setRole(Role.ADMIN);
                usuarioRepository.save(user);
                log.info("Perfil ADMIN garantido para o usuário {}", emailNormalizado);
            }
            return;
        }

        Estabelecimento central = Estabelecimento.builder()
                .nome("Central Maré Alerta")
                .tipoNegocio(TipoNegocio.OUTROS)
                .responsavel("Administrador do Sistema")
                .bairro(Bairro.CAMPINA)
                .logradouro("Centro Operacional")
                .numero("S/N")
                .municipio("Belém")
                .uf("PA")
                .ativo(true)
                .build();
        estabelecimentoRepository.save(central);

        Usuario admin = new Usuario();
        admin.setEmail(emailNormalizado);
        admin.setSenha(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        admin.setEstabelecimento(central);
        usuarioRepository.save(admin);

        log.info("Usuário ADMIN inicial criado: {}", emailNormalizado);
    }
}
