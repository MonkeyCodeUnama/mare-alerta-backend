package com.marealerta.backend.estabelecimento.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {

    Optional<Estabelecimento> findByDocumento(String documento);

    List<Estabelecimento> findByTipoNegocio(TipoNegocio tipoNegocio);

    List<Estabelecimento> findByBairro(Bairro bairro);

    List<Estabelecimento> findByBairroAndTipoNegocio(Bairro bairro, TipoNegocio tipoNegocio);

    List<Estabelecimento> findByAtivoTrue();
}
