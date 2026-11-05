package com.marealerta.backend.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;
public record WhatsappTestRequest(@NotBlank String telefone, @NotBlank String mensagem) {}
