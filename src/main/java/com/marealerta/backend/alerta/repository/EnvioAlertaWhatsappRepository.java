package com.marealerta.backend.alerta.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marealerta.backend.alerta.model.EnvioAlertaWhatsapp;
import com.marealerta.backend.alerta.model.StatusEnvioWhatsapp;

public interface EnvioAlertaWhatsappRepository extends JpaRepository<EnvioAlertaWhatsapp, Long> {

    List<EnvioAlertaWhatsapp> findByAlertaId(Long alertaId);

    List<EnvioAlertaWhatsapp> findByStatusEnvioOrderByCriadoEmAsc(StatusEnvioWhatsapp statusEnvio);

    List<EnvioAlertaWhatsapp> findByEstabelecimentoIdOrderByCriadoEmDesc(Long estabelecimentoId);
}
