package com.marealerta.backend.telemetria.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serializa a sincronizacao de telemetria entre requisicoes e instancias.
 *
 * <p>A sincronizacao consulta o Open-Meteo e faz find-or-create das leituras. Sem
 * serializacao, duas requisicoes concorrentes ao dashboard tentam inserir a mesma
 * chave (estacao, fonte, tipo, data_hora) e uma delas falha com violacao do indice
 * unico uq_telemetria_chave. O advisory lock transacional do PostgreSQL e liberado
 * automaticamente no commit/rollback.</p>
 */
@Component
@RequiredArgsConstructor
public class TelemetriaSyncLock {

    private static final long LOCK_KEY = 0x4D415245L;

    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public void adquirir() {
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:chave)")
                .setParameter("chave", LOCK_KEY)
                .getSingleResult();
    }
}
