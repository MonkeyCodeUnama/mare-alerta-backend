package com.marealerta.backend.estabelecimento.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marealerta.backend.estabelecimento.model.EstabelecimentoHorario;

public interface EstabelecimentoHorarioRepository extends JpaRepository<EstabelecimentoHorario, Long> {
    
    List<EstabelecimentoHorario> findByEstabelecimentoId(Long estabelecimentoId);

    List<EstabelecimentoHorario> findByEstabelecimentoIdAndDiaSemana(Long estabelecimentoId, Integer diaSemana);
}
