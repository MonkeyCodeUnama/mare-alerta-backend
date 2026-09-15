package com.marealerta.backend.ocorrencias.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.marealerta.backend.ocorrencias.model.Ocorrencia;

import java.math.BigDecimal;
import java.util.List;


public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    List<Ocorrencia> findByDataFimIsNull();

    @Query("""
        SELECT o FROM Ocorrencia o 
        WHERE o.latitude BETWEEN :latMin AND :latMax 
          AND o.longitude BETWEEN :lonMin AND :lonMax
        ORDER BY o.dataInicio DESC
    """)
    List<Ocorrencia> findByBoundingBox(
        @Param("latMin") BigDecimal latMin,
        @Param("latMax") BigDecimal latMax,
        @Param("lonMin") BigDecimal lonMin,
        @Param("lonMax") BigDecimal lonMax
    );
}
