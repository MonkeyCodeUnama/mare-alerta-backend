package com.marealerta.backend.whatsapp.dto;

import java.time.Instant;
public record WhatsappHistoricoResponse(Long id, String telefone, String status, String erro, Instant criadoEm, Instant enviadoEm) {}
