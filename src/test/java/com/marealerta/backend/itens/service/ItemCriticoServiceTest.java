package com.marealerta.backend.itens.service;

import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.itens.dto.ItemCriticoRequest;
import com.marealerta.backend.itens.dto.ItemCriticoResponse;
import com.marealerta.backend.itens.model.ItemCritico;
import com.marealerta.backend.itens.repository.ItensCriticosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCriticoServiceTest {

    @Mock
    private ItensCriticosRepository itensCriticosRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @InjectMocks
    private ItemCriticoService itemCriticoService;

    private Estabelecimento estabelecimento;
    private ItemCritico itemCritico;
    private ItemCriticoRequest request;

    @BeforeEach
    void setUp() {
        estabelecimento = Estabelecimento.builder()
                .id(1L)
                .nome("Padaria do Ver-o-Peso")
                .build();

        itemCritico = ItemCritico.builder()
                .id(10L)
                .nome("Freezer Industrial")
                .descricao("No piso térreo")
                .estabelecimento(estabelecimento)
                .build();

        request = ItemCriticoRequest.builder()
                .estabelecimentoId(1L)
                .nome("Freezer Industrial")
                .descricao("No piso térreo")
                .build();
    }

    @Test
    @DisplayName("Deve criar item critico com sucesso")
    void deveCriarItemCriticoComSucesso() {
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimento));
        when(itensCriticosRepository.save(any(ItemCritico.class))).thenReturn(itemCritico);

        ItemCriticoResponse response = itemCriticoService.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getNome()).isEqualTo("Freezer Industrial");
        assertThat(response.getDescricao()).isEqualTo("No piso térreo");
        assertThat(response.getEstabelecimentoId()).isEqualTo(1L);

        verify(itensCriticosRepository).save(any(ItemCritico.class));
    }

    @Test
    @DisplayName("Deve lancar excecao ao criar item critico sem estabelecimentoId")
    void deveLancarExcecaoAoCriarSemEstabelecimentoId() {
        ItemCriticoRequest requestSemId = ItemCriticoRequest.builder()
                .nome("Freezer")
                .build();

        assertThatThrownBy(() -> itemCriticoService.criar(requestSemId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do estabelecimento é obrigatório");

        verify(itensCriticosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao ao criar item critico com estabelecimento inexistente")
    void deveLancarExcecaoAoCriarComEstabelecimentoInexistente() {
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemCriticoService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estabelecimento não encontrado");

        verify(itensCriticosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar itens criticos de um estabelecimento")
    void deveListarItensCriticos() {
        when(itensCriticosRepository.findByEstabelecimentoId(1L)).thenReturn(List.of(itemCritico));

        List<ItemCriticoResponse> lista = itemCriticoService.listar(1L);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getNome()).isEqualTo("Freezer Industrial");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando estabelecimentoId for nulo")
    void deveRetornarListaVaziaQuandoEstabelecimentoIdNulo() {
        List<ItemCriticoResponse> lista = itemCriticoService.listar(null);

        assertThat(lista).isEmpty();
        verify(itensCriticosRepository, never()).findByEstabelecimentoId(any());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando estabelecimento nao possuir itens cadastrados")
    void deveRetornarListaVaziaQuandoNaoHouverItens() {
        when(itensCriticosRepository.findByEstabelecimentoId(1L)).thenReturn(List.of());

        List<ItemCriticoResponse> lista = itemCriticoService.listar(1L);

        assertThat(lista).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar item critico por ID")
    void deveBuscarPorIdComSucesso() {
        when(itensCriticosRepository.findById(10L)).thenReturn(Optional.of(itemCritico));

        ItemCriticoResponse response = itemCriticoService.buscarPorId(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getNome()).isEqualTo("Freezer Industrial");
    }

    @Test
    @DisplayName("Deve lancar excecao ao buscar item critico inexistente")
    void deveLancarExcecaoAoBuscarInexistente() {
        when(itensCriticosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemCriticoService.buscarPorId(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item crítico não encontrado");
    }

    @Test
    @DisplayName("Deve atualizar item critico com sucesso")
    void deveAtualizarItemCriticoComSucesso() {
        ItemCriticoRequest requestAtualizado = ItemCriticoRequest.builder()
                .nome("Freezer Atualizado")
                .descricao("Elevado 30cm")
                .build();

        ItemCritico itemAtualizado = ItemCritico.builder()
                .id(10L)
                .nome("Freezer Atualizado")
                .descricao("Elevado 30cm")
                .estabelecimento(estabelecimento)
                .build();

        when(itensCriticosRepository.findById(10L)).thenReturn(Optional.of(itemCritico));
        when(itensCriticosRepository.save(any(ItemCritico.class))).thenReturn(itemAtualizado);

        ItemCriticoResponse response = itemCriticoService.atualizar(10L, requestAtualizado);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo("Freezer Atualizado");
        assertThat(response.getDescricao()).isEqualTo("Elevado 30cm");
    }

    @Test
    @DisplayName("Deve deletar item critico com sucesso")
    void deveDeletarItemCriticoComSucesso() {
        when(itensCriticosRepository.existsById(10L)).thenReturn(true);
        doNothing().when(itensCriticosRepository).deleteById(10L);

        itemCriticoService.deletar(10L);

        verify(itensCriticosRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Deve lancar excecao ao deletar item inexistente")
    void deveLancarExcecaoAoDeletarInexistente() {
        when(itensCriticosRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> itemCriticoService.deletar(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item crítico não encontrado");

        verify(itensCriticosRepository, never()).deleteById(any());
    }
}
