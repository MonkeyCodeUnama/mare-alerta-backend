package com.marealerta.backend.whatsapp.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "configuracao_whatsapp") @Getter @Setter @NoArgsConstructor
public class ConfiguracaoWhatsapp {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String instancia;
    @Column(name = "telefone_remetente", length = 30) private String telefoneRemetente;
    @Column(nullable = false) private boolean ativo = true;
    @Column(name = "enviar_alertas_automaticos", nullable = false) private boolean enviarAlertasAutomaticos = true;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;
    @PrePersist void criarDatas() { criadoEm = atualizadoEm = Instant.now(); }
    @PreUpdate void atualizarData() { atualizadoEm = Instant.now(); }
}
