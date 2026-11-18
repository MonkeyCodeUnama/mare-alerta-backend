package com.marealerta.backend.whatsapp.dto;

import java.time.Instant;

public record WhatsappHistoricoResponse(
        Long id,
        String telefone,
        String status,
        String erro,
        Instant criadoEm,
        Instant enviadoEm,
        String estabelecimentoNome,
        String responsavel
) {
    public WhatsappHistoricoResponse(
            Long id,
            String telefone,
            String status,
            String erro,
            Instant criadoEm,
            Instant enviadoEm
    ) {
        this(id, telefone, status, erro, criadoEm, enviadoEm, null, null);
    }
}
