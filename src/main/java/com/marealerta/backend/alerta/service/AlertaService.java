package com.marealerta.backend.alerta.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marealerta.backend.alerta.client.EvolutionApiClient;
import com.marealerta.backend.alerta.client.EvolutionSendResponse;
import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.model.Alerta;
import com.marealerta.backend.alerta.model.EnvioAlertaWhatsapp;
import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.alerta.model.OrigemAlerta;
import com.marealerta.backend.alerta.model.StatusEnvioWhatsapp;
import com.marealerta.backend.alerta.model.TipoAlerta;
import com.marealerta.backend.alerta.repository.AlertaRepository;
import com.marealerta.backend.alerta.repository.EnvioAlertaWhatsappRepository;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final EnvioAlertaWhatsappRepository envioAlertaWhatsappRepository;
    private final EvolutionApiClient evolutionApiClient;

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarTodos() {
        return alertaRepository.findAll(Sort.by(Sort.Direction.DESC, "dataEnvio"))
                .stream()
                .map(AlertaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarPorEstabelecimento(Long estabelecimentoId) {
        return alertaRepository.findByEstabelecimentoIdOrderByDataEnvioDesc(estabelecimentoId)
                .stream()
                .map(AlertaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertaResponse buscarPorId(Long id) {
        return alertaRepository.findById(id)
                .map(AlertaResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Alerta não encontrado com ID: " + id));
    }

    @Transactional
    public AlertaResponse criarAlerta(AlertaRequest request) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(request.getEstabelecimentoId())
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com ID: " + request.getEstabelecimentoId()));

        Alerta alerta = new Alerta();
        alerta.setEstabelecimento(estabelecimento);
        alerta.setNivelRisco(request.getNivelRisco());
        alerta.setMensagem(request.getMensagem());
        alerta.setTipoAlerta(request.getTipoAlerta());
        alerta.setOrigemAlerta(request.getOrigemAlerta() != null ? request.getOrigemAlerta() : OrigemAlerta.AUTOMATICO);

        Alerta salvo = alertaRepository.save(alerta);

        processarEnvioWhatsapp(salvo, estabelecimento);

        return AlertaResponse.fromEntity(salvo);
    }

    @Transactional
    public AlertaResponse enviarAlerta(AlertaRequest request) {
        return criarAlerta(request);
    }

    @Transactional
    public List<AlertaResponse> dispararAlertaBairro(
            Bairro bairro,
            NivelRisco nivelRisco,
            String mensagem,
            TipoAlerta tipoAlerta,
            OrigemAlerta origemAlerta
    ) {
        List<Estabelecimento> estabelecimentos = estabelecimentoRepository.findByBairroAndAtivoTrue(bairro);
        OrigemAlerta origem = origemAlerta != null ? origemAlerta : OrigemAlerta.AUTOMATICO;

        return estabelecimentos.stream().map(estab -> {
            Alerta alerta = new Alerta();
            alerta.setEstabelecimento(estab);
            alerta.setNivelRisco(nivelRisco);
            alerta.setMensagem(mensagem);
            alerta.setTipoAlerta(tipoAlerta);
            alerta.setOrigemAlerta(origem);

            Alerta salvo = alertaRepository.save(alerta);
            processarEnvioWhatsapp(salvo, estab);
            return AlertaResponse.fromEntity(salvo);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<EnvioAlertaWhatsapp> listarEnviosWhatsapp(Long alertaId) {
        return envioAlertaWhatsappRepository.findByAlertaId(alertaId);
    }

    private void processarEnvioWhatsapp(Alerta alerta, Estabelecimento estabelecimento) {
        String telefone = estabelecimento.getTelefone();
        if (telefone == null || telefone.isBlank()) {
            return;
        }

        String telefoneFormatado = EvolutionApiClient.formatarTelefoneE164(telefone);

        EnvioAlertaWhatsapp envio = EnvioAlertaWhatsapp.builder()
                .alerta(alerta)
                .estabelecimento(estabelecimento)
                .telefoneDestinatario(telefoneFormatado)
                .statusEnvio(StatusEnvioWhatsapp.PENDENTE)
                .tentativas(0)
                .criadoEm(Instant.now())
                .build();

        envio = envioAlertaWhatsappRepository.save(envio);

        try {
            String mensagemFormatada = formatarMensagemWhatsapp(alerta, estabelecimento);
            EvolutionSendResponse response = evolutionApiClient.enviarTexto(telefoneFormatado, mensagemFormatada);

            envio.setStatusEnvio(StatusEnvioWhatsapp.ENVIADO);
            envio.setWhatsappMessageId(response != null ? response.getMessageId() : null);
            envio.setEnviadoEm(Instant.now());
            envio.setTentativas(1);
        } catch (Exception ex) {
            log.warn("Falha no disparo de WhatsApp para estabelecimento {}: {}", estabelecimento.getId(), ex.getMessage());
            envio.setStatusEnvio(StatusEnvioWhatsapp.FALHA);
            envio.setTentativas(1);
            envio.setErroMensagem(ex.getMessage());
        }

        envioAlertaWhatsappRepository.save(envio);
    }

    private String formatarMensagemWhatsapp(Alerta alerta, Estabelecimento estabelecimento) {
        return String.format(
                "🚨 *MARÉ ALERTA — DEFESA CIVIL*\n\n" +
                "⚠️ *Risco:* %s\n" +
                "📍 *Bairro:* %s\n" +
                "🏪 *Estabelecimento:* %s\n\n" +
                "📢 *Comunicado:* %s",
                alerta.getNivelRisco(),
                estabelecimento.getBairro() != null ? estabelecimento.getBairro().getDescricao() : "Belém",
                estabelecimento.getNome(),
                alerta.getMensagem()
        );
    }
}
