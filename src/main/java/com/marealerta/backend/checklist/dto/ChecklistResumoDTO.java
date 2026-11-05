package com.marealerta.backend.checklist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistResumoDTO {

    private long totalItens;
    private long concluidos;
    private long pendentes;
    private int percentualConcluido;
    private String progressoFormatado;
    private boolean prontoParaInundacao;
}
