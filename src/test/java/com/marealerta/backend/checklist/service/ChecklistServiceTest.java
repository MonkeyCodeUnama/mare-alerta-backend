package com.marealerta.backend.checklist.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marealerta.backend.checklist.dto.ChecklistResumoDTO;
import com.marealerta.backend.checklist.dto.ItemChecklistRequest;
import com.marealerta.backend.checklist.dto.ItemChecklistResponse;
import com.marealerta.backend.checklist.model.ItemChecklist;
import com.marealerta.backend.checklist.model.PrioridadeChecklist;
import com.marealerta.backend.checklist.model.StatusChecklist;
import com.marealerta.backend.checklist.repository.ItemChecklistRepository;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private ItemChecklistRepository itemChecklistRepository;

    @InjectMocks
    private ChecklistService checklistService;

    private Estabelecimento estabelecimento;
    private ItemChecklist item;

    @BeforeEach
    void setUp() {
        estabelecimento = Estabelecimento.builder()
                .id(1L)
                .nome("Mercado Central")
                .tipoNegocio(TipoNegocio.MERCADO)
                .ativo(true)
                .build();

        item = ItemChecklist.builder()
                .id(10L)
                .estabelecimento(estabelecimento)
                .titulo("Elevar freezers")
                .descricao("Subir equipamentos elétricos 50cm")
                .categoria("ESTOQUE")
                .status(StatusChecklist.PENDENTE)
                .prioridade(PrioridadeChecklist.ALTA)
                .ordem(1)
                .build();
    }

    @Test
    @DisplayName("Deve criar item de checklist com sucesso")
    void deveCriarItemChecklist() {
        ItemChecklistRequest request = ItemChecklistRequest.builder()
                .estabelecimentoId(1L)
                .titulo("Elevar freezers")
                .descricao("Subir equipamentos elétricos 50cm")
                .categoria("ESTOQUE")
                .status(StatusChecklist.PENDENTE)
                .prioridade(PrioridadeChecklist.ALTA)
                .ordem(1)
                .build();

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimento));
        when(itemChecklistRepository.save(any(ItemChecklist.class))).thenReturn(item);

        ItemChecklistResponse response = checklistService.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitulo()).isEqualTo("Elevar freezers");
        verify(itemChecklistRepository).save(any(ItemChecklist.class));
    }

    @Test
    @DisplayName("Deve listar itens de checklist por estabelecimento e status")
    void deveListarItensChecklist() {
        when(itemChecklistRepository.findByEstabelecimentoIdAndStatusOrderByOrdemAsc(1L, StatusChecklist.PENDENTE))
                .thenReturn(List.of(item));

        List<ItemChecklistResponse> lista = checklistService.listar(1L, StatusChecklist.PENDENTE);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve buscar item de checklist por ID")
    void deveBuscarPorId() {
        when(itemChecklistRepository.findById(10L)).thenReturn(Optional.of(item));

        ItemChecklistResponse response = checklistService.buscarPorId(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar ID inexistente")
    void deveLancarExcecaoQuandoIdNaoExistir() {
        when(itemChecklistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checklistService.buscarPorId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item de checklist não encontrado");
    }

    @Test
    @DisplayName("Deve atualizar item de checklist com sucesso")
    void deveAtualizarItemChecklist() {
        ItemChecklistRequest request = ItemChecklistRequest.builder()
                .titulo("Elevar freezers atualizado")
                .status(StatusChecklist.CONCLUIDO)
                .prioridade(PrioridadeChecklist.ALTA)
                .build();

        when(itemChecklistRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemChecklistRepository.save(any(ItemChecklist.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemChecklistResponse response = checklistService.atualizar(10L, request);

        assertThat(response).isNotNull();
        assertThat(response.getTitulo()).isEqualTo("Elevar freezers atualizado");
        assertThat(response.getStatus()).isEqualTo(StatusChecklist.CONCLUIDO);
    }

    @Test
    @DisplayName("Deve alternar status de pendente para concluido")
    void deveAlternarStatusParaConcluido() {
        when(itemChecklistRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemChecklistRepository.save(any(ItemChecklist.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemChecklistResponse response = checklistService.alternarStatus(10L);

        assertThat(response.getStatus()).isEqualTo(StatusChecklist.CONCLUIDO);
    }

    @Test
    @DisplayName("Deve alternar status de concluido de volta para pendente")
    void deveAlternarStatusParaPendente() {
        item.setStatus(StatusChecklist.CONCLUIDO);
        when(itemChecklistRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemChecklistRepository.save(any(ItemChecklist.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemChecklistResponse response = checklistService.alternarStatus(10L);

        assertThat(response.getStatus()).isEqualTo(StatusChecklist.PENDENTE);
    }

    @Test
    @DisplayName("Deve marcar item como concluido")
    void deveMarcarComoConcluido() {
        when(itemChecklistRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemChecklistRepository.save(any(ItemChecklist.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemChecklistResponse response = checklistService.marcarComoConcluido(10L);

        assertThat(response.getStatus()).isEqualTo(StatusChecklist.CONCLUIDO);
    }

    @Test
    @DisplayName("Deve obter resumo estatístico de progresso do checklist")
    void deveObterResumoComProgressoCalculado() {
        when(itemChecklistRepository.countByEstabelecimentoId(1L)).thenReturn(5L);
        when(itemChecklistRepository.countByEstabelecimentoIdAndStatus(1L, StatusChecklist.CONCLUIDO)).thenReturn(3L);

        ChecklistResumoDTO resumo = checklistService.obterResumo(1L);

        assertThat(resumo).isNotNull();
        assertThat(resumo.getTotalItens()).isEqualTo(5L);
        assertThat(resumo.getConcluidos()).isEqualTo(3L);
        assertThat(resumo.getPendentes()).isEqualTo(2L);
        assertThat(resumo.getPercentualConcluido()).isEqualTo(60);
        assertThat(resumo.getProgressoFormatado()).isEqualTo("60%");
        assertThat(resumo.isProntoParaInundacao()).isFalse();
    }

    @Test
    @DisplayName("Deve indicar prontoParaInundacao verdadeiro quando 100% dos itens estiverem concluidos")
    void deveIndicarProntoQuandoTodosConcluidos() {
        when(itemChecklistRepository.countByEstabelecimentoId(1L)).thenReturn(4L);
        when(itemChecklistRepository.countByEstabelecimentoIdAndStatus(1L, StatusChecklist.CONCLUIDO)).thenReturn(4L);

        ChecklistResumoDTO resumo = checklistService.obterResumo(1L);

        assertThat(resumo.getPercentualConcluido()).isEqualTo(100);
        assertThat(resumo.getProgressoFormatado()).isEqualTo("100%");
        assertThat(resumo.isProntoParaInundacao()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar resumo zerado quando estabelecimentoId for nulo")
    void deveRetornarResumoZeradoQuandoIdNulo() {
        ChecklistResumoDTO resumo = checklistService.obterResumo(null);

        assertThat(resumo.getTotalItens()).isEqualTo(0L);
        assertThat(resumo.getPercentualConcluido()).isEqualTo(0);
        assertThat(resumo.getProgressoFormatado()).isEqualTo("0%");
        assertThat(resumo.isProntoParaInundacao()).isFalse();
    }

    @Test
    @DisplayName("Deve gerar itens padrao para estabelecimento de mercado")
    void deveGerarItensPadraoMercado() {
        when(itemChecklistRepository.existsByEstabelecimentoIdAndTitulo(eq(1L), any())).thenReturn(false);

        checklistService.gerarItensPadraoParaEstabelecimento(estabelecimento);

        verify(itemChecklistRepository, atLeastOnce()).save(any(ItemChecklist.class));
    }

    @Test
    @DisplayName("Deve deletar item de checklist com sucesso")
    void deveDeletarItemChecklist() {
        when(itemChecklistRepository.existsById(10L)).thenReturn(true);

        checklistService.deletar(10L);

        verify(itemChecklistRepository).deleteById(10L);
    }
}
