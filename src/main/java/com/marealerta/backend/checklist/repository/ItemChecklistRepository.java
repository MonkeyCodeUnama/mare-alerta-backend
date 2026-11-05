package com.marealerta.backend.checklist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marealerta.backend.checklist.model.ItemChecklist;
import com.marealerta.backend.checklist.model.PrioridadeChecklist;
import com.marealerta.backend.checklist.model.StatusChecklist;

public interface ItemChecklistRepository extends JpaRepository<ItemChecklist, Long> {

    List<ItemChecklist> findByEstabelecimentoIdOrderByOrdemAsc(Long estabelecimentoId);

    List<ItemChecklist> findByEstabelecimentoIdAndStatusOrderByOrdemAsc(Long estabelecimentoId, StatusChecklist status);

    List<ItemChecklist> findByEstabelecimentoIdAndPrioridadeOrderByOrdemAsc(Long estabelecimentoId, PrioridadeChecklist prioridade);

    long countByEstabelecimentoIdAndStatus(Long estabelecimentoId, StatusChecklist status);

    boolean existsByEstabelecimentoIdAndTitulo(Long estabelecimentoId, String titulo);
}
