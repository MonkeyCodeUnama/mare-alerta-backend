package com.marealerta.backend.ocorrencias.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.marealerta.backend.estabelecimento.model.Estabelecimento;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ocorrencias")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String protocolo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id")
    private Estabelecimento estabelecimento;

    @Column(length = 150)
    private String titulo;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 50)
    private StatusOcorrencia status = StatusOcorrencia.RASCUNHO;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, name = "nivel_alagamento")
    private NivelAlagamento nivelAlagamento;

    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDate dataOcorrencia;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    @Column(name = "faixa_lamina_agua", length = 50)
    private String faixaLaminaAgua;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "categorias_dano")
    private String categoriasDano;

    @Column(name = "valor_estimado_prejuizo", precision = 12, scale = 2)
    private BigDecimal valorEstimadoPrejuizo;

    @Column(name = "horas_fechado")
    private Integer horasFechado;

    @Column(name = "tamanho_equipe")
    private Integer tamanhoEquipe;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcorrenciaFoto> fotos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusOcorrencia.RASCUNHO;
        }
        if (this.dataOcorrencia == null) {
            this.dataOcorrencia = LocalDate.now();
        }
        if (this.horaInicio == null) {
            this.horaInicio = LocalTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
