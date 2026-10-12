package com.marealerta.backend.itens.model;

import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.marealerta.backend.estabelecimento.model.Estabelecimento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "itens_criticos")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ItemCritico {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "estabelecimento_id" )
    private Estabelecimento estabelecimento;
}
