package com.marealerta.backend.checklist.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final EstabelecimentoRepository estabelecimentoRepository;
    private final ItemChecklistRepository itemChecklistRepository;

    @Transactional
    public ItemChecklistResponse criar(ItemChecklistRequest itemChecklistRequest) {
        if (itemChecklistRequest.getEstabelecimentoId() == null) {
            throw new IllegalArgumentException("O ID do estabelecimento é obrigatório");
        }

        Estabelecimento estab = estabelecimentoRepository.findById(itemChecklistRequest.getEstabelecimentoId())
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com ID: " + itemChecklistRequest.getEstabelecimentoId()));

        ItemChecklist itemChecklist = ItemChecklist.builder()
                .estabelecimento(estab)
                .titulo(itemChecklistRequest.getTitulo())
                .descricao(itemChecklistRequest.getDescricao())
                .categoria(itemChecklistRequest.getCategoria())
                .status(itemChecklistRequest.getStatus() != null ? itemChecklistRequest.getStatus() : StatusChecklist.PENDENTE)
                .prioridade(itemChecklistRequest.getPrioridade() != null ? itemChecklistRequest.getPrioridade() : PrioridadeChecklist.MEDIA)
                .ordem(itemChecklistRequest.getOrdem() != null ? itemChecklistRequest.getOrdem() : 0)
                .build();

        itemChecklist = itemChecklistRepository.save(itemChecklist);

        return ItemChecklistResponse.fromEntity(itemChecklist);
    }

    @Transactional(readOnly = true)
    public List<ItemChecklistResponse> listar(Long estabelecimentoId, StatusChecklist statusChecklist) {
        if (estabelecimentoId == null) {
            return List.of();
        }

        List<ItemChecklist> itens;

        if (statusChecklist != null) {
            itens = itemChecklistRepository.findByEstabelecimentoIdAndStatusOrderByOrdemAsc(estabelecimentoId, statusChecklist);
        } else {
            itens = itemChecklistRepository.findByEstabelecimentoIdOrderByOrdemAsc(estabelecimentoId);
        }

        return itens.stream()
                .map(ItemChecklistResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemChecklistResponse buscarPorId(Long id) {
        ItemChecklist item = itemChecklistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item de checklist não encontrado com ID: " + id));
        return ItemChecklistResponse.fromEntity(item);
    }

    @Transactional
    public ItemChecklistResponse atualizar(Long id, ItemChecklistRequest request) {
        ItemChecklist item = itemChecklistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item de checklist não encontrado com ID: " + id));

        if (request.getTitulo() != null && !request.getTitulo().isBlank()) {
            item.setTitulo(request.getTitulo());
        }
        if (request.getDescricao() != null) {
            item.setDescricao(request.getDescricao());
        }
        if (request.getCategoria() != null) {
            item.setCategoria(request.getCategoria());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        if (request.getPrioridade() != null) {
            item.setPrioridade(request.getPrioridade());
        }
        if (request.getOrdem() != null) {
            item.setOrdem(request.getOrdem());
        }

        item = itemChecklistRepository.save(item);

        return ItemChecklistResponse.fromEntity(item);
    }

    @Transactional
    public void deletar(Long id) {
        if (!itemChecklistRepository.existsById(id)) {
            throw new IllegalArgumentException("Item de checklist não encontrado com ID: " + id);
        }
        itemChecklistRepository.deleteById(id);
    }

    @Transactional
    public ItemChecklistResponse alternarStatus(Long id) {
        ItemChecklist item = itemChecklistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item de checklist não encontrado com ID: " + id));

        StatusChecklist novoStatus = item.getStatus() == StatusChecklist.CONCLUIDO
                ? StatusChecklist.PENDENTE
                : StatusChecklist.CONCLUIDO;
        item.setStatus(novoStatus);

        item = itemChecklistRepository.save(item);
        return ItemChecklistResponse.fromEntity(item);
    }

    @Transactional
    public ItemChecklistResponse marcarComoConcluido(Long id) {
        ItemChecklist item = itemChecklistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item de checklist não encontrado com ID: " + id));

        item.setStatus(StatusChecklist.CONCLUIDO);
        item = itemChecklistRepository.save(item);
        return ItemChecklistResponse.fromEntity(item);
    }

    @Transactional(readOnly = true)
    public ChecklistResumoDTO obterResumo(Long estabelecimentoId) {
        if (estabelecimentoId == null) {
            return ChecklistResumoDTO.builder()
                    .totalItens(0)
                    .concluidos(0)
                    .pendentes(0)
                    .percentualConcluido(0)
                    .progressoFormatado("0%")
                    .prontoParaInundacao(false)
                    .build();
        }

        long total = itemChecklistRepository.countByEstabelecimentoId(estabelecimentoId);
        long concluidos = itemChecklistRepository.countByEstabelecimentoIdAndStatus(estabelecimentoId, StatusChecklist.CONCLUIDO);
        long pendentes = Math.max(0, total - concluidos);

        int percentual = total > 0 ? (int) Math.round(((double) concluidos / total) * 100) : 0;
        String formatado = percentual + "%";
        boolean pronto = total > 0 && concluidos == total;

        return ChecklistResumoDTO.builder()
                .totalItens(total)
                .concluidos(concluidos)
                .pendentes(pendentes)
                .percentualConcluido(percentual)
                .progressoFormatado(formatado)
                .prontoParaInundacao(pronto)
                .build();
    }

    @Transactional
    public List<ItemChecklistResponse> gerarItensPadrao(Long estabelecimentoId) {
        if (estabelecimentoId == null) {
            return List.of();
        }

        Estabelecimento estab = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com ID: " + estabelecimentoId));

        gerarItensPadraoParaEstabelecimento(estab);

        return listar(estabelecimentoId, null);
    }

    @Transactional
    public void gerarItensPadraoParaEstabelecimento(Estabelecimento estab) {
        if (estab == null || estab.getId() == null) {
            return;
        }

        List<ItemPadraoDef> templates = obterTemplatesPorTipo(estab.getTipoNegocio());
        for (ItemPadraoDef def : templates) {
            if (!itemChecklistRepository.existsByEstabelecimentoIdAndTitulo(estab.getId(), def.titulo())) {
                ItemChecklist item = ItemChecklist.builder()
                        .estabelecimento(estab)
                        .titulo(def.titulo())
                        .descricao(def.descricao())
                        .categoria(def.categoria())
                        .status(StatusChecklist.PENDENTE)
                        .prioridade(def.prioridade())
                        .ordem(def.ordem())
                        .build();
                itemChecklistRepository.save(item);
            }
        }
    }

    private List<ItemPadraoDef> obterTemplatesPorTipo(TipoNegocio tipo) {
        TipoNegocio seguro = tipo != null ? tipo : TipoNegocio.OUTROS;
        List<ItemPadraoDef> itens = new ArrayList<>();

        switch (seguro) {
            case MERCADO, VAREJO -> {
                itens.add(new ItemPadraoDef("Elevar freezers e refrigeradores", "Subir equipamentos refrigerados sobre paletes ou suportes de pelo menos 30 a 40cm do chão.", "EQUIPAMENTOS", PrioridadeChecklist.ALTA, 1));
                itens.add(new ItemPadraoDef("Remover mercadorias das prateleiras baixas", "Subir sacarias de grãos, farináceos e caixas de papelão para níveis acima de 60cm.", "ESTOQUE", PrioridadeChecklist.ALTA, 2));
                itens.add(new ItemPadraoDef("Desligar disjuntores de circuitos baixos", "Desenergizar tomadas e circuitos próximos ao solo no painel elétrico principal.", "SEGURANCA", PrioridadeChecklist.ALTA, 3));
                itens.add(new ItemPadraoDef("Instalar comportas de contenção nas portas", "Fixar chapas metálicas ou barreiras com vedação de borracha nas entradas.", "INFRAESTRUTURA", PrioridadeChecklist.MEDIA, 4));
                itens.add(new ItemPadraoDef("Realizar backup do PDV e arquivar documentos", "Salvar dados fiscais do sistema e guardar documentos fiscais em local elevado ou nuvem.", "ADMINISTRATIVO", PrioridadeChecklist.MEDIA, 5));
            }
            case PADARIA -> {
                itens.add(new ItemPadraoDef("Elevar sacarias de farinha e insumos secos", "Armazenar farinha de trigo, sal e açúcar sobre estrados de pelo menos 50cm.", "ESTOQUE", PrioridadeChecklist.ALTA, 1));
                itens.add(new ItemPadraoDef("Desconectar fornos e masseiras industriais", "Desligar e isolar conexões elétricas de motores de misturadores e fornos industriais.", "EQUIPAMENTOS", PrioridadeChecklist.ALTA, 2));
                itens.add(new ItemPadraoDef("Bloquear registro central de gás GLP", "Fechar a válvula da bateria de cilindros de gás para prevenir acidentes durante enchentes.", "SEGURANCA", PrioridadeChecklist.ALTA, 3));
                itens.add(new ItemPadraoDef("Proteger vitrines e balcões de atendimento", "Instalar barreiras de contenção nas portas frontais e elevar balcões refrigerados.", "INFRAESTRUTURA", PrioridadeChecklist.MEDIA, 4));
            }
            case FARMACIA -> {
                itens.add(new ItemPadraoDef("Realocar medicamentos das gavetas e prateleiras baixas", "Transferir medicamentos controlados e termolábeis para prateleiras superiores.", "ESTOQUE", PrioridadeChecklist.ALTA, 1));
                itens.add(new ItemPadraoDef("Proteger refrigerador de vacinas e termolábeis", "Assegurar nobreak elevado e vedação da porta para manter temperatura controlada.", "EQUIPAMENTOS", PrioridadeChecklist.ALTA, 2));
                itens.add(new ItemPadraoDef("Proteger livros de registro e receitas retidas", "Guardar receituários e documentações sanitárias em caixas plásticas herméticas no alto.", "ADMINISTRATIVO", PrioridadeChecklist.ALTA, 3));
                itens.add(new ItemPadraoDef("Desconectar computadores e nobreaks de solo", "Elevar CPUs, servidores de retaguarda e nobreaks para cima de mesas.", "TI", PrioridadeChecklist.MEDIA, 4));
            }
            case RESTAURANTE, LANCHONETE, ALIMENTACAO -> {
                itens.add(new ItemPadraoDef("Trancar câmaras frias e proteger motores de solo", "Trancar portas frigoríficas e elevar motores ou condensadoras instalados no térreo.", "EQUIPAMENTOS", PrioridadeChecklist.ALTA, 1));
                itens.add(new ItemPadraoDef("Vedar ralos de chão e caixas de gordura", "Instalar tampões herméticos nos ralos para impedir refluxo de esgoto durante a maré alta.", "INFRAESTRUTURA", PrioridadeChecklist.ALTA, 2));
                itens.add(new ItemPadraoDef("Suspender mesas, cadeiras e estoque seco", "Empilhar cadeiras e elevar caixas de bebidas e mantimentos secos.", "ESTOQUE", PrioridadeChecklist.MEDIA, 3));
                itens.add(new ItemPadraoDef("Fechar válvula principal de gás industrial", "Interromper o fornecimento de gás para evitar vazamentos na área de cocção.", "SEGURANCA", PrioridadeChecklist.ALTA, 4));
            }
            case OFICINA -> {
                itens.add(new ItemPadraoDef("Elevar veículos e peças nos elevadores", "Subir elevadores automotivos com veículos e motores em manutenção acima de 1 metro.", "EQUIPAMENTOS", PrioridadeChecklist.ALTA, 1));
                itens.add(new ItemPadraoDef("Lacrar e elevar tambores de óleo e solventes", "Fechar recipientes de fluidos, graxas e solventes para evitar contaminação da água.", "MEIO_AMBIENTE", PrioridadeChecklist.ALTA, 2));
                itens.add(new ItemPadraoDef("Recolher ferramentas pneumáticas e elétricas", "Armazenar ferramentas elétricas e equipamentos de diagnóstico em prateleiras superiores.", "ESTOQUE", PrioridadeChecklist.MEDIA, 3));
            }
            case SALAO_BELEZA, BELEZA -> {
                itens.add(new ItemPadraoDef("Desconectar secadores, pranchas e autoclaves", "Remover plugues de tomadas baixas e apoiar aparelhos elétricos em bancadas superiores.", "EQUIPAMENTOS", PrioridadeChecklist.ALTA, 1));
                itens.add(new ItemPadraoDef("Elevar cosméticos e produtos químicos", "Guardar tinturas, pós descolorantes e químicos em armários aéreos.", "ESTOQUE", PrioridadeChecklist.MEDIA, 2));
                itens.add(new ItemPadraoDef("Proteger bases de cadeiras hidráulicas e lavatórios", "Colocar capas protetoras plásticas e vedar ralos dos lavatórios.", "INFRAESTRUTURA", PrioridadeChecklist.MEDIA, 3));
            }
            default -> {
                itens.add(new ItemPadraoDef("Instalar barreiras de contenção nas entradas", "Posicionar comportas ou sacos de contenção para retardar invasão da maré.", "INFRAESTRUTURA", PrioridadeChecklist.ALTA, 1));
                itens.add(new ItemPadraoDef("Elevar equipamentos eletrônicos e computadores", "Retirar nobreaks, cabos e fiações de áreas suscetíveis ao alagamento.", "TI", PrioridadeChecklist.ALTA, 2));
                itens.add(new ItemPadraoDef("Desligar disjuntor de energia geral", "Cortar fornecimento elétrico de pavimentos inundáveis antes do horário de pico da maré.", "SEGURANCA", PrioridadeChecklist.ALTA, 3));
                itens.add(new ItemPadraoDef("Acondicionar documentos e valores em local estanque", "Guardar contratos, notas fiscais e valores em recipientes impermeáveis em nível elevado.", "ADMINISTRATIVO", PrioridadeChecklist.MEDIA, 4));
            }
        }
        return itens;
    }

    private record ItemPadraoDef(String titulo, String descricao, String categoria, PrioridadeChecklist prioridade, int ordem) {}
}
