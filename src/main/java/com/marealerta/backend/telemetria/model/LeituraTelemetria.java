package com.marealerta.backend.telemetria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leituras_telemetria")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeituraTelemetria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estacao_codigo", nullable = false, length = 50)
    private String estacaoCodigo;

    @Column(name = "estacao_nome", length = 150)
    private String estacaoNome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_registro", nullable = false, length = 50)
    private TipoRegistroTelemetria tipoRegistro;

    @Column(name = "nivel_mare", precision = 5, scale = 2)
    private BigDecimal nivelMare;

    @Column(precision = 5, scale = 2)
    private BigDecimal precipitacao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
