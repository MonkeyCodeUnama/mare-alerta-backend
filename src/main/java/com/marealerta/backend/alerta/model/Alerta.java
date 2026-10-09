package com.marealerta.backend.alerta.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.marealerta.backend.estabelecimento.model.Estabelecimento;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alerta")
@Getter 
@Setter
@NoArgsConstructor 
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_risco", nullable = false, length = 20)
    private NivelRisco nivelRisco;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false, length = 30)
    private TipoAlerta tipoAlerta;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_alerta", nullable = false, length = 20)
    private OrigemAlerta origemAlerta;

    @Column(nullable = false, name = "data_envio", updatable = false)
    private LocalDateTime dataEnvio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "estabelecimento_id")
    private Estabelecimento estabelecimento;

    @PrePersist()
    public void PrePersist() {
        this.dataEnvio = LocalDateTime.now();
    }
}
