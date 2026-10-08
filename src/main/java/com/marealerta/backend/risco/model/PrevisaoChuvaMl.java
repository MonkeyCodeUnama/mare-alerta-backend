package com.marealerta.backend.risco.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "previsoes_chuva_ml",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_previsao_ml_emissao_alvo",
            columnNames = {"estacao_codigo", "versao_modelo", "instante_emissao", "instante_alvo"}
        )
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrevisaoChuvaMl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estacao_codigo", nullable = false, length = 50)
    private String estacaoCodigo;

    @Column(name = "instante_emissao", nullable = false)
    private Instant instanteEmissao;

    @Column(name = "instante_alvo", nullable = false)
    private Instant instanteAlvo;

    @Column(name = "horizonte_horas", nullable = false)
    private Integer horizonteHoras;

    @Column(name = "probabilidade_chuva", nullable = false)
    private Double probabilidadeChuva;

    @Column(name = "chuva_esperada", nullable = false)
    private Boolean chuvaEsperada;

    @Column(name = "precipitacao_estimada_mm", precision = 6, scale = 2)
    private BigDecimal precipitacaoEstimadaMm;

    @Column(name = "versao_modelo", nullable = false, length = 50)
    private String versaoModelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_qualidade", nullable = false, length = 30)
    private StatusQualidadeMl statusQualidade;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
}

