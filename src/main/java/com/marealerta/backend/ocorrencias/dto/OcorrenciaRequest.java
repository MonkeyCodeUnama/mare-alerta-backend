package com.marealerta.backend.ocorrencias.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.marealerta.backend.ocorrencias.model.NivelAlagamento;
import com.marealerta.backend.ocorrencias.model.StatusOcorrencia;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OcorrenciaRequest {

    private Long estabelecimentoId;

    private String titulo;

    private String descricao;

    @Builder.Default
    private StatusOcorrencia status = StatusOcorrencia.RASCUNHO;

    private NivelAlagamento nivelAlagamento;

    @NotNull(message = "A data da ocorrência é obrigatória")
    private LocalDate dataOcorrencia;

    @NotNull(message = "A hora de início é obrigatória")
    private LocalTime horaInicio;

    private LocalTime horaFim;

    private String faixaLaminaAgua;

    private String categoriasDano;

    private BigDecimal valorEstimadoPrejuizo;

    private Integer horasFechado;

    private Integer tamanhoEquipe;
}
