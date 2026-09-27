package com.marealerta.backend.estabelecimento.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class HorarioRequest {

    @NotNull
    private String diaSemana;

    @NotNull
    private LocalTime horarioAbertura;

    @NotNull
    private LocalTime horarioFechamento;
}
