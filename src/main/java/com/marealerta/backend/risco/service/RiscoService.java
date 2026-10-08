package com.marealerta.backend.risco.service;

import com.marealerta.backend.risco.client.OpenMeteoClient;
import com.marealerta.backend.risco.dto.PrevisaoMlRequest;
import com.marealerta.backend.risco.dto.PrevisaoMlResponse;
import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.risco.model.PrevisaoChuvaMl;
import com.marealerta.backend.risco.model.StatusQualidadeMl;
import com.marealerta.backend.risco.repository.PrevisaoChuvaMlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class RiscoService {

    private final MlApiCliente mlApiClient;
    private final OpenMeteoClient openMeteoClient;
    private final PrevisaoChuvaMlRepository previsaoChuvaMlRepository;

    @Transactional
    public RiscoResponse processarEObterRiscoParaLocalidade(String localidade) {
        // Coordenadas de referência para Belém
        double lat = -1.4558;
        double lon = -48.5044;

        // 1. Busca os dados crus da Open-Meteo
        OpenMeteoClient.DadosMeteo dadosMeteo;
        try {
            dadosMeteo = openMeteoClient.buscarDadosAtuais(lat, lon, localidade);
        } catch (RuntimeException e) {
            throw new ExternalRiskServiceException("serviço de meteorologia indisponível", e);
        }

        // 2. Envia o conjunto de features, nomes, unidades e horário previstos pelo modelo.
        PrevisaoMlRequest request = new PrevisaoMlRequest(
                "rain-hourly-v1",
                BigDecimal.valueOf(lat),
                BigDecimal.valueOf(lon),
                dadosMeteo.featureTime().toString(),
                1,
                dadosMeteo.features(),
                dadosMeteo.units()
        );

        return processarEObterRisco(request, localidade);
    }

    @Transactional
    public RiscoResponse processarEObterRisco(PrevisaoMlRequest request, String localidade) {
        PrevisaoMlResponse responseMl;
        try {
            responseMl = mlApiClient.solicitarPrevisao(request);
        } catch (RuntimeException e) {
            throw new ExternalRiskServiceException("serviço de previsão de risco indisponível", e);
        }

        String estacao = (localidade != null && !localidade.isBlank()) ? localidade : "BELEM_CENTRO";
        Instant instanteEmissao = responseMl.getIssuedAt() != null ? responseMl.getIssuedAt() : Instant.now();
        Instant instanteAlvo = responseMl.getTargetTime() != null
                ? responseMl.getTargetTime()
                : (responseMl.getIssuedAt() != null ? responseMl.getIssuedAt() : Instant.now());
        Integer horizonteHoras = responseMl.getHorizonHours() != null ? responseMl.getHorizonHours() : 1;

        PrevisaoChuvaMl entidade = PrevisaoChuvaMl.builder()
                .estacaoCodigo(estacao)
                .instanteEmissao(instanteEmissao)
                .instanteAlvo(instanteAlvo)
                .horizonteHoras(horizonteHoras)
                .probabilidadeChuva(responseMl.getRainProbability())
                .chuvaEsperada(responseMl.getRainExpected())
                .precipitacaoEstimadaMm(responseMl.getExpectedPrecipitationMm())
                .versaoModelo(responseMl.getModelVersion() != null ? responseMl.getModelVersion() : "v1.0.0")
                .statusQualidade(responseMl.getQualityStatus() != null ? responseMl.getQualityStatus() : StatusQualidadeMl.OK)
                .criadoEm(Instant.now())
                .build();

        previsaoChuvaMlRepository.save(entidade);

        NivelRisco nivelRisco = NivelRisco.BAIXO;
        if (responseMl.getRainProbability() != null) {
            if (responseMl.getRainProbability() >= 0.75) {
                nivelRisco = NivelRisco.ALTO;
            } else if (responseMl.getRainProbability() >= 0.40) {
                nivelRisco = NivelRisco.MEDIO;
            }
        }

        LocalDateTime horarioPico = instanteAlvo.atZone(ZoneId.systemDefault()).toLocalDateTime();

        return RiscoResponse.builder()
                .bairro(estacao)
                .nivelRisco(nivelRisco)
                .probabilidadeChuva(responseMl.getRainProbability())
                .nivelMarePrevistoMetros(null)
                .descricaoCenario(Boolean.TRUE.equals(responseMl.getRainExpected())
                        ? "Alta probabilidade de chuva prevista para o período."
                        : "Sem previsão significativa de chuva.")
                .horarioPicoPrevisto(horarioPico)
                .alertaEmitido(nivelRisco == NivelRisco.ALTO)
                .build();
    }
}
