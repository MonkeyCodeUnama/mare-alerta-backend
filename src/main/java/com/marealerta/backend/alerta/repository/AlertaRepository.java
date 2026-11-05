package com.marealerta.backend.alerta.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marealerta.backend.alerta.model.Alerta;
import com.marealerta.backend.alerta.model.OrigemAlerta;
import com.marealerta.backend.alerta.model.TipoAlerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findAllByOrderByDataEnvioDesc();
    
    List<Alerta> findByEstabelecimentoIdOrderByDataEnvioDesc(Long estabelecimentoId);
    
    List<Alerta> findByEstabelecimentoIdAndTipoAlerta(Long estabelecimentoId, TipoAlerta tipoAlerta);

    List<Alerta> findByEstabelecimentoIdAndOrigemAlerta(Long estabelecimentoId, OrigemAlerta origemAlerta);
}
