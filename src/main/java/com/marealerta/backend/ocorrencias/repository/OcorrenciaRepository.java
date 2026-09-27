package com.marealerta.backend.ocorrencias.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marealerta.backend.ocorrencias.model.Ocorrencia;
import com.marealerta.backend.ocorrencias.model.StatusOcorrencia;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    Optional<Ocorrencia> findByProtocolo(String protocolo);

    List<Ocorrencia> findByEstabelecimentoIdOrderByDataOcorrenciaDescHoraInicioDesc(Long estabelecimentoId);

    List<Ocorrencia> findByStatusOrderByDataOcorrenciaDesc(StatusOcorrencia status);

    List<Ocorrencia> findByEstabelecimentoIdAndStatus(Long estabelecimentoId, StatusOcorrencia status);

    long countByEstabelecimentoId(Long estabelecimentoId);
}
