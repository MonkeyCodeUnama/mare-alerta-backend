package com.marealerta.backend.estabelecimento.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class HorarioResponse {
    private String diaSemana;
    private LocalTime horarioAbertura;
    private LocalTime horarioFechamento;
}
