package com.marealerta.backend.estabelecimento.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marealerta.backend.estabelecimento.dto.EstabelecimentoAtualizacaoRequest;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.model.EstabelecimentoHorario;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoHorarioRepository;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.itens.model.ItemCritico;
import com.marealerta.backend.itens.repository.ItensCriticosRepository;
import com.marealerta.backend.usuario.model.Usuario;
import com.marealerta.backend.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class EstabelecimentoServiceTest {

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EstabelecimentoHorarioRepository estabelecimentoHorarioRepository;

    @Mock
    private ItensCriticosRepository itensCriticosRepository;

    @InjectMocks
    private EstabelecimentoService estabelecimentoService;

    private Estabelecimento estabelecimento;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        estabelecimento = Estabelecimento.builder()
                .id(1L)
                .nome("Padaria Belem")
                .documento("12345678000195")
                .tipoNegocio(TipoNegocio.PADARIA)
                .responsavel("Carlos")
                .bairro(Bairro.CAMPINA)
                .logradouro("Rua dos Frades")
                .numero("100")
                .municipio("Belém")
                .uf("PA")
                .telefone("91988887777")
                .ativo(true)
                .build();

        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("carlos@padaria.com");
        usuario.setEstabelecimento(estabelecimento);
    }

    @Test
    void deveBuscarEstabelecimentoPorIdComSucesso() {
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimento));
        when(estabelecimentoHorarioRepository.findByEstabelecimentoId(1L)).thenReturn(List.of());
        when(itensCriticosRepository.findByEstabelecimentoId(1L)).thenReturn(List.of());

        EstabelecimentoResponse response = estabelecimentoService.buscarPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Padaria Belem");
        assertThat(response.getBairro()).isEqualTo(Bairro.CAMPINA);
    }

    @Test
    void deveLancarExcecaoAoBuscarPorIdInexistente() {
        when(estabelecimentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estabelecimentoService.buscarPorId(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estabelecimento não encontrado com ID: 99");
    }

    @Test
    void deveBuscarPorEmailUsuarioComSucesso() {
        when(usuarioRepository.findByEmail("carlos@padaria.com")).thenReturn(Optional.of(usuario));
        when(estabelecimentoHorarioRepository.findByEstabelecimentoId(1L)).thenReturn(List.of());
        when(itensCriticosRepository.findByEstabelecimentoId(1L)).thenReturn(List.of());

        EstabelecimentoResponse response = estabelecimentoService.buscarPorEmailUsuario("carlos@padaria.com");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Padaria Belem");
    }

    @Test
    void deveLancarExcecaoAoBuscarPorEmailInexistente() {
        when(usuarioRepository.findByEmail("desconhecido@exemplo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estabelecimentoService.buscarPorEmailUsuario("desconhecido@exemplo.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário não encontrado com e-mail");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoTemEstabelecimentoVinculado() {
        Usuario usuarioSemEstab = new Usuario();
        usuarioSemEstab.setEmail("solteiro@exemplo.com");

        when(usuarioRepository.findByEmail("solteiro@exemplo.com")).thenReturn(Optional.of(usuarioSemEstab));

        assertThatThrownBy(() -> estabelecimentoService.buscarPorEmailUsuario("solteiro@exemplo.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário não possui estabelecimento vinculado");
    }

    @Test
    void deveListarTodosOsEstabelecimentosAtivos() {
        when(estabelecimentoRepository.findByAtivoTrue()).thenReturn(List.of(estabelecimento));
        when(estabelecimentoHorarioRepository.findByEstabelecimentoId(1L)).thenReturn(List.of());
        when(itensCriticosRepository.findByEstabelecimentoId(1L)).thenReturn(List.of());

        List<EstabelecimentoResponse> lista = estabelecimentoService.listarTodos();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getNome()).isEqualTo("Padaria Belem");
    }

    @Test
    void deveAtualizarEstabelecimentoComHorariosEItensCriticos() {
        EstabelecimentoAtualizacaoRequest request = EstabelecimentoAtualizacaoRequest.builder()
                .nome("Padaria Belem Atualizada")
                .responsavel("Carlos Silva")
                .bairro(Bairro.MARCO)
                .logradouro("Avenida Almirante Barroso")
                .numero("500")
                .tipoNegocio(TipoNegocio.PADARIA)
                .horarioAbertura("06:00")
                .horarioFechamento("20:00")
                .dias(List.of("Seg", "Ter", "Qua"))
                .itensCriticos(List.of("Forno a lenha", "Freezer vertical"))
                .build();

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimento));

        EstabelecimentoHorario h1 = new EstabelecimentoHorario();
        h1.setDiaSemana(2);
        h1.setHorarioAbertura(LocalTime.of(6, 0));
        h1.setHorarioFechamento(LocalTime.of(20, 0));

        ItemCritico ic1 = new ItemCritico();
        ic1.setNome("Forno a lenha");

        when(estabelecimentoHorarioRepository.findByEstabelecimentoId(1L)).thenReturn(List.of(h1));
        when(itensCriticosRepository.findByEstabelecimentoId(1L)).thenReturn(List.of(ic1));

        EstabelecimentoResponse response = estabelecimentoService.atualizar(1L, request);

        assertThat(response).isNotNull();
        assertThat(estabelecimento.getNome()).isEqualTo("Padaria Belem Atualizada");
        assertThat(estabelecimento.getResponsavel()).isEqualTo("Carlos Silva");
        assertThat(estabelecimento.getBairro()).isEqualTo(Bairro.MARCO);

        verify(estabelecimentoRepository).save(estabelecimento);
        verify(estabelecimentoHorarioRepository).deleteByEstabelecimentoId(1L);
        verify(itensCriticosRepository).deleteByEstabelecimentoId(1L);
    }
}

