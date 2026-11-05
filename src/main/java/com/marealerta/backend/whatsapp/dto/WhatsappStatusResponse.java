package com.marealerta.backend.whatsapp.dto;

public record WhatsappStatusResponse(String instancia, String estado, String telefone, String nome, String foto, boolean ativo, boolean enviarAlertasAutomaticos) {}
