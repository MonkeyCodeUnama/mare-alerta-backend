package com.marealerta.backend.estabelecimento.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {
    
    Optional<Estabelecimento> findByDocumento(String documento);

    List<Estabelecimento> findByTipoNegocio(TipoNegocio tipoNegocio);

    @Query("""
        SELECT e FROM Estabelecimento e 
        WHERE e.latitude BETWEEN :latMin AND :latMax 
          AND e.longitude BETWEEN :lonMin AND :lonMax
    """)
    List<Estabelecimento> findByBoundingBox(
        @Param("latMin") BigDecimal latMin, 
        @Param("latMax") BigDecimal latMax, 
        @Param("lonMin") BigDecimal lonMin, 
        @Param("lonMax") BigDecimal lonMax
    );
}
