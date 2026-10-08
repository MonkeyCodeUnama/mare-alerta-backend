package com.marealerta.backend.alerta.model;

import java.time.Instant;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "envios_alerta_whatsapp")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvioAlertaWhatsapp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alerta_id", nullable = false)
    private Alerta alerta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @Column(name = "telefone_destinatario", nullable = false, length = 20)
    private String telefoneDestinatario;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_envio", nullable = false, length = 30)
    private StatusEnvioWhatsapp statusEnvio;

    @Column(name = "whatsapp_message_id", length = 100)
    private String whatsappMessageId;

    @Column(nullable = false)
    private Integer tentativas;

    @Column(name = "erro_mensagem", columnDefinition = "TEXT")
    private String erroMensagem;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "enviado_em")
    private Instant enviadoEm;
}
