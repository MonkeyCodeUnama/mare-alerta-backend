package com.marealerta.backend.alerta;

import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.model.Alerta;
import com.marealerta.backend.alerta.model.EnvioAlertaWhatsapp;
import com.marealerta.backend.alerta.model.StatusEnvioWhatsapp;
import com.marealerta.backend.alerta.repository.AlertaRepository;
import com.marealerta.backend.alerta.repository.EnvioAlertaWhatsappRepository;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.ocorrencias.model.Ocorrencia;
import com.marealerta.backend.ocorrencias.repository.OcorrenciaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.marealerta.backend.alerta.model.Alerta;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlertaService {

    @Value("${WHATSAPP_API_URL}")
    private String apiUrl;

    @Value("${WHATSAPP_API_KEY")
    private String apiKey;

    @Value("${WHATSAPP_INSTANCIA")
    private String instancia;

    private final EstabelecimentoRepository estabelecimentoRepository;
    private final AlertaRepository alertaRepository;
    private final RestClient restClient;
    private final EnvioAlertaWhatsappRepository envioAlertaWhatsappRepository;

    public AlertaResponse enviarAlerta(AlertaRequest alertaRequest) throws BadRequestException {
     Estabelecimento estab= estabelecimentoRepository.findById(alertaRequest.getEstabelecimentoId())
                .orElseThrow(() -> new EntityNotFoundException("ocorrencia nao encontrada"));
        if(estab == null){
            throw new BadRequestException();
        }

        Alerta alerta = new Alerta();
        alerta.setEstabelecimento(estab);
        alerta.setNivelRisco(alertaRequest.getNivelRisco());
        alerta.setMensagem(alertaRequest.getMensagem());
        alerta.setTipoAlerta(alertaRequest.getTipoAlerta());
        alerta.setOrigemAlerta(alertaRequest.getOrigemAlerta());

        alerta = alertaRepository.save(alerta);

        EnvioAlertaWhatsapp envio = EnvioAlertaWhatsapp.builder()
                .alerta(alerta)
                .estabelecimento(estab)
                .telefoneDestinatario(estab.getTelefone())
                .statusEnvio(StatusEnvioWhatsapp.PENDENTE)
                .tentativas(1)
                .criadoEm(Instant.now())
                .build();

        envio = envioAlertaWhatsappRepository.save(envio);

        try{
            dispararWhatsapp(estab.getTelefone(), alertaRequest.getMensagem());

            envio.setStatusEnvio(StatusEnvioWhatsapp.ENVIADO);
            envio.setEnviadoEm(Instant.now());
        }catch (Exception e){
            envio.setStatusEnvio(StatusEnvioWhatsapp.FALHA);
            envio.setErroMensagem(e.getMessage());
        }
        envioAlertaWhatsappRepository.save(envio);

        return AlertaResponse.fromEntity(alerta);

    }

    private void dispararWhatsapp(String telefone, String mensagem){
        String endpoint = apiUrl + "/message/send/Text" + instancia;

        restClient.post()
                .uri(endpoint)
                .header("apiKey", apiKey)
                .body(Map.of(
                        "number", telefone,
                        "text", mensagem
                ))
                .retrieve()
                .toBodilessEntity();
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarPorEstabelecimento(Long estabelecimentoId) {
        return alertaRepository.findByEstabelecimentoIdOrderByDataEnvioDesc(estabelecimentoId)
                .stream()
                .map(AlertaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertaResponse buscarPorId(Long id){
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alerta nao encontrado"));
        return AlertaResponse.fromEntity(alerta);
    }

}
