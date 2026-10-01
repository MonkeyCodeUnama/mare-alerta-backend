package com.marealerta.backend.ocorrencias.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import com.marealerta.backend.ocorrencias.model.NivelAlagamento;
import com.marealerta.backend.ocorrencias.model.Ocorrencia;
import com.marealerta.backend.ocorrencias.model.StatusOcorrencia;

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
public class OcorrenciaResponse {

    private Long id;
    private String protocolo;
    private Long estabelecimentoId;
    private String estabelecimentoNome;
    private String titulo;
    private String descricao;
    private StatusOcorrencia status;
    private NivelAlagamento nivelAlagamento;
    private LocalDate dataOcorrencia;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String faixaLaminaAgua;
    private String categoriasDano;
    private BigDecimal valorEstimadoPrejuizo;
    private Integer horasFechado;
    private Integer tamanhoEquipe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OcorrenciaFotoResponse> fotos;

    public static OcorrenciaResponse fromEntity(Ocorrencia o) {
        if (o == null) return null;
        List<OcorrenciaFotoResponse> fotoResponses = o.getFotos() != null
                ? o.getFotos().stream().map(OcorrenciaFotoResponse::fromEntity).toList()
                : Collections.emptyList();

        return OcorrenciaResponse.builder()
                .id(o.getId())
                .protocolo(o.getProtocolo())
                .estabelecimentoId(o.getEstabelecimento() != null ? o.getEstabelecimento().getId() : null)
                .estabelecimentoNome(o.getEstabelecimento() != null ? o.getEstabelecimento().getNome() : null)
                .titulo(o.getTitulo())
                .descricao(o.getDescricao())
                .status(o.getStatus())
                .nivelAlagamento(o.getNivelAlagamento())
                .dataOcorrencia(o.getDataOcorrencia())
                .horaInicio(o.getHoraInicio())
                .horaFim(o.getHoraFim())
                .faixaLaminaAgua(o.getFaixaLaminaAgua())
                .categoriasDano(o.getCategoriasDano())
                .valorEstimadoPrejuizo(o.getValorEstimadoPrejuizo())
                .horasFechado(o.getHorasFechado())
                .tamanhoEquipe(o.getTamanhoEquipe())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .fotos(fotoResponses)
                .build();
    }
}
