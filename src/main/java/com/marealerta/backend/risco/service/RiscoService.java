package com.marealerta.backend.risco.service;

import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.risco.dto.PrevisaoMlRequest;
import com.marealerta.backend.risco.dto.PrevisaoMlResponse;
import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.risco.model.PrevisaoChuvaMl;
import com.marealerta.backend.risco.model.StatusQualidadeMl;
import com.marealerta.backend.risco.repository.PrevisaoChuvaMlRepository;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.service.OpenMeteoClient;
import com.marealerta.backend.telemetria.service.TelemetriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiscoService {

    private final MlApiCliente mlApiClient;
    private final PrevisaoChuvaMlRepository previsaoChuvaMlRepository;
    private final TelemetriaService telemetriaService;
    private final OpenMeteoClient openMeteoClient;

    @Transactional
    public RiscoResponse processarEObterRiscoParaLocalidade(String localidade) {
        double lat = -1.4558;
        double lon = -48.5044;

        TelemetriaDashboardResponse telemetria = telemetriaService.obterDashboard();
        OpenMeteoClient.GfsFeaturesSnapshot gfsSnapshot = openMeteoClient.buscarFeaturesGfsAtuais(lat, lon);

        PrevisaoMlRequest request = new PrevisaoMlRequest(
                "rain-hourly-v1",
                BigDecimal.valueOf(lat),
                BigDecimal.valueOf(lon),
                gfsSnapshot.featureTime(),
                1,
                gfsSnapshot.features(),
                gfsSnapshot.units()
        );

        return processarEObterRisco(request, localidade, telemetria);
    }

    @Transactional
    public RiscoResponse processarEObterRisco(PrevisaoMlRequest request, String localidade, TelemetriaDashboardResponse telemetria) {
        if (telemetria == null) {
            telemetria = telemetriaService.obterDashboard();
        }

        PrevisaoMlResponse responseMl = mlApiClient.solicitarPrevisao(request);
        StatusQualidadeMl statusQualidade = (responseMl.getQualityStatus() != null) ? responseMl.getQualityStatus() : StatusQualidadeMl.OK;

        String estacao = (localidade != null && !localidade.isBlank()) ? localidade : "BELEM_CENTRO";
        Instant instanteEmissao = responseMl.getIssuedAt() != null ? responseMl.getIssuedAt() : Instant.now();
        Instant instanteAlvo = responseMl.getTargetTime() != null ? responseMl.getTargetTime() : Instant.now().plus(1, ChronoUnit.HOURS);
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
                .statusQualidade(statusQualidade)
                .criadoEm(Instant.now())
                .build();

        try {
            previsaoChuvaMlRepository.save(entidade);
        } catch (Exception ex) {
            log.warn("Falha ao salvar historico de previsao ML: {}", ex.getMessage());
        }

        BigDecimal nivelMare = telemetria.getNivelAtual();
        Double probChuva = responseMl.getRainProbability();

        NivelRisco nivelRisco = calcularRiscoCombinado(nivelMare, probChuva);
        LocalDateTime horarioPico = instanteAlvo.atZone(ZoneId.of("America/Belem")).toLocalDateTime();
        String descricao = gerarDescricaoCenario(nivelRisco, probChuva, nivelMare);

        return RiscoResponse.builder()
                .bairro(estacao)
                .nivelRisco(nivelRisco)
                .probabilidadeChuva(probChuva)
                .nivelMarePrevistoMetros(nivelMare)
                .descricaoCenario(descricao)
                .horarioPicoPrevisto(horarioPico)
                .alertaEmitido(nivelRisco == NivelRisco.ALTO)
                .build();
    }

    private NivelRisco calcularRiscoCombinado(BigDecimal nivelMare, Double probabilidadeChuva) {
        double mare = nivelMare != null ? nivelMare.doubleValue() : 0.0;
        double prob = probabilidadeChuva != null ? probabilidadeChuva : 0.0;

        if ((mare >= 3.0 && prob >= 0.50) || prob >= 0.85 || mare >= 3.5) {
            return NivelRisco.ALTO;
        }
        if (mare >= 2.5 || prob >= 0.40) {
            return NivelRisco.MEDIO;
        }
        return NivelRisco.BAIXO;
    }

    private String gerarDescricaoCenario(NivelRisco risco, Double probChuva, BigDecimal nivelMare) {
        if (risco == NivelRisco.ALTO) {
            return String.format("ALERTA CRÍTICO: Combinação de maré elevada (%.2fm) com alta probabilidade de chuva (%.0f%%).",
                    nivelMare != null ? nivelMare.doubleValue() : 0.0,
                    probChuva != null ? probChuva * 100 : 0.0);
        } else if (risco == NivelRisco.MEDIO) {
            return String.format("ATENÇÃO: Nível da maré em %.2fm e probabilidade de chuva em %.0f%%.",
                    nivelMare != null ? nivelMare.doubleValue() : 0.0,
                    probChuva != null ? probChuva * 100 : 0.0);
        }
        return "Condições normais. Sem risco significativo de alagamento.";
    }
}
