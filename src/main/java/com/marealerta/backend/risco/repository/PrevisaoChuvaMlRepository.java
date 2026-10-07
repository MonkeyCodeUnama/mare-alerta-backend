package com.marealerta.backend.risco.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.marealerta.backend.risco.model.PrevisaoChuvaMl;

public interface PrevisaoChuvaMlRepository extends JpaRepository<PrevisaoChuvaMl, Long> {

    @Query("""
        SELECT p FROM PrevisaoChuvaMl p
        WHERE p.estacaoCodigo = :estacaoCodigo
          AND p.instanteAlvo BETWEEN :inicio AND :fim
        ORDER BY p.instanteAlvo ASC, p.instanteEmissao DESC
    """)
    List<PrevisaoChuvaMl> buscarPrevisoesJanela(
        @Param("estacaoCodigo") String estacaoCodigo,
        @Param("inicio") Instant inicio,
        @Param("fim") Instant fim
    );

    @Query("""
        SELECT p FROM PrevisaoChuvaMl p
        WHERE p.estacaoCodigo = :estacaoCodigo
          AND p.instanteAlvo >= :instanteReferencia
        ORDER BY p.instanteEmissao DESC, p.instanteAlvo ASC
        LIMIT 1
    """)
    Optional<PrevisaoChuvaMl> buscarUltimaPrevisaoValida(
        @Param("estacaoCodigo") String estacaoCodigo,
        @Param("instanteReferencia") Instant instanteReferencia
    );

    List<PrevisaoChuvaMl> findByEstacaoCodigoAndInstanteAlvoOrderByInstanteEmissaoDesc(
        String estacaoCodigo,
        Instant instanteAlvo
    );
}
