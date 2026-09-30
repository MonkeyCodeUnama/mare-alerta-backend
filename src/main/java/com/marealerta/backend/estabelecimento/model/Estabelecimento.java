package com.marealerta.backend.estabelecimento.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estabelecimento")
@SQLDelete(sql = "UPDATE estabelecimento SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 20)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_negocio", nullable = false, length = 50)
    private TipoNegocio tipoNegocio;

    @Column(nullable = false, length = 150)
    private String responsavel;

    //@Enumerated(EnumType.STRING)
    @Convert(converter = ConversorBairro.class)
    @Column(nullable = false, length = 50)
    private Bairro bairro;

    @Column(nullable = false, length = 255)
    private String logradouro;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 10)
    private String cep;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(length = 20)
    private String telefone;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.ativo == null) {
            this.ativo = true;
        }
        if (this.bairro == null) {
            this.bairro = Bairro.MARCO;
        }
        if (this.municipio == null) {
            this.municipio = "Belém";
        }
        if (this.uf == null) {
            this.uf = "PA";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
