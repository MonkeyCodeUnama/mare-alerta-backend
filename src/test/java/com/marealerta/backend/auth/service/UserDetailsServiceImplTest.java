package com.marealerta.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.marealerta.backend.usuario.model.Usuario;
import com.marealerta.backend.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void deveCarregarUsuarioPorEmailComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("maria@doca.com");
        usuario.setSenha("hash-senha");

        when(usuarioRepository.findByEmail("maria@doca.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername("maria@doca.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("maria@doca.com");
        assertThat(userDetails.getPassword()).isEqualTo("hash-senha");
        verify(usuarioRepository).findByEmail("maria@doca.com");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmail("inexistente@exemplo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("inexistente@exemplo.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("usuario nao encontrado");

        verify(usuarioRepository).findByEmail("inexistente@exemplo.com");
    }
}

