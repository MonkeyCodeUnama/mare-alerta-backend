package com.marealerta.backend.ocorrencias.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marealerta.backend.ocorrencias.model.OcorrenciaFoto;

public interface OcorrenciaFotoRepository extends JpaRepository<OcorrenciaFoto, Long> {

    List<OcorrenciaFoto> findByOcorrenciaId(Long ocorrenciaId);

    List<OcorrenciaFoto> findByOcorrenciaProtocolo(String protocolo);
}
