package com.marealerta.backend.risco.service;

import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.estabelecimento.model.Bairro;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

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
        Bairro bairro = Bairro.fromString(localidade);
        double lat = (bairro != null && bairro.getLatitude() != null) ? bairro.getLatitude() : -1.4558;
        double lon = (bairro != null && bairro.getLongitude() != null) ? bairro.getLongitude() : -48.5044;

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

        return processarEObterRiscoInterno(request, localidade, telemetria, bairro, lat, lon);
    }

    @Transactional
    public RiscoResponse processarEObterRisco(PrevisaoMlRequest request, String localidade, TelemetriaDashboardResponse telemetria) {
        Bairro bairro = Bairro.fromString(localidade);
        double lat = request.latitude() != null ? request.latitude().doubleValue() : -1.4558;
        double lon = request.longitude() != null ? request.longitude().doubleValue() : -48.5044;
        return processarEObterRiscoInterno(request, localidade, telemetria, bairro, lat, lon);
    }

    private RiscoResponse processarEObterRiscoInterno(
            PrevisaoMlRequest request,
            String localidade,
            TelemetriaDashboardResponse telemetria,
            Bairro bairro,
            double lat,
            double lon) {

        if (telemetria == null) {
            telemetria = telemetriaService.obterDashboard();
        }

        PrevisaoMlResponse responseMl = mlApiClient.solicitarPrevisao(request);
        StatusQualidadeMl statusQualidade = (responseMl.getQualityStatus() != null) ? responseMl.getQualityStatus() : StatusQualidadeMl.OK;

        String estacao = (localidade != null && !localidade.isBlank()) ? localidade : "BELEM_CENTRO";
        Instant instanteEmissao = responseMl.getIssuedAt() != null ? responseMl.getIssuedAt() : Instant.now();
        Instant instanteAlvo = responseMl.getTargetTime() != null ? responseMl.getTargetTime() : Instant.now().plus(1, ChronoUnit.HOURS);
        Integer horizonteHoras = responseMl.getHorizonHours() != null ? responseMl.getHorizonHours() : 1;

        Double probMl = responseMl.getRainProbability() != null ? responseMl.getRainProbability() : 0.0;
        Double probCalibrada = calibrarProbabilidade(probMl, lat, lon);

        double probArredondada = BigDecimal.valueOf(probCalibrada).setScale(2, RoundingMode.HALF_UP).doubleValue();
        int probPercentual = (int) Math.round(probArredondada * 100);
        String probFormatada = probPercentual + "%";

        PrevisaoChuvaMl entidade = PrevisaoChuvaMl.builder()
                .estacaoCodigo(estacao)
                .instanteEmissao(instanteEmissao)
                .instanteAlvo(instanteAlvo)
                .horizonteHoras(horizonteHoras)
                .probabilidadeChuva(probArredondada)
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

        BigDecimal nivelMare = telemetria.getNivelAtual() != null
                ? telemetria.getNivelAtual().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        String cotaFormatada = String.format(Locale.US, "%.2f m", nivelMare.doubleValue());

        Double cotaTransbordamento = (bairro != null && bairro.getCotaTransbordamentoMetros() != null)
                ? bairro.getCotaTransbordamentoMetros()
                : 3.00;

        String statusCota;
        if (nivelMare.doubleValue() >= cotaTransbordamento) {
            statusCota = "TRANSBORDAMENTO";
        } else if (nivelMare.doubleValue() >= (cotaTransbordamento - 0.30)) {
            statusCota = "ALERTA";
        } else {
            statusCota = "NORMAL";
        }

        NivelRisco nivelRisco = calcularRiscoTopografico(bairro, nivelMare.doubleValue(), probArredondada);
        LocalDateTime horarioPico = instanteAlvo.atZone(ZoneId.of("America/Belem")).toLocalDateTime();
        String descricao = gerarDescricaoCenario(nivelRisco, probArredondada, nivelMare, bairro);

        return RiscoResponse.builder()
                .bairro(estacao)
                .nivelRisco(nivelRisco)
                .probabilidadeChuva(probArredondada)
                .probabilidadePercentual(probPercentual)
                .probabilidadeFormatada(probFormatada)
                .nivelMarePrevistoMetros(nivelMare)
                .cotaFormatada(cotaFormatada)
                .cotaTransbordamentoMetros(cotaTransbordamento)
                .statusCota(statusCota)
                .descricaoCenario(descricao)
                .horarioPicoPrevisto(horarioPico)
                .alertaEmitido(nivelRisco == NivelRisco.ALTO)
                .build();
    }

    private Double calibrarProbabilidade(Double probMl, double lat, double lon) {
        try {
            OpenMeteoClient.OpenMeteoResponse response = openMeteoClient.buscarDadosAtuaisEPrevisao(lat, lon);
            if (response != null && response.hourly() != null && response.hourly().probabilidadePrecipitacao() != null && !response.hourly().probabilidadePrecipitacao().isEmpty()) {
                BigDecimal probEnsemble = response.hourly().probabilidadePrecipitacao().get(0);
                if (probEnsemble != null) {
                    double pEnsemble = probEnsemble.doubleValue() / 100.0;
                    return (pEnsemble * 0.70) + (probMl * 0.30);
                }
            }
        } catch (Exception ex) {
            log.debug("Ensemble Open-Meteo nao consultado ou indisponivel: {}", ex.getMessage());
        }
        return probMl;
    }

    private NivelRisco calcularRiscoTopografico(Bairro bairro, double mare, double prob) {
        double cota = (bairro != null && bairro.getCotaTransbordamentoMetros() != null)
                ? bairro.getCotaTransbordamentoMetros()
                : 3.00;

        boolean isBairroAlto = cota >= 6.00;

        if (!isBairroAlto) {
            boolean transbordando = mare >= cota;
            boolean quaseTransbordando = mare >= (cota - 0.20);
            boolean cotaAlerta = mare >= (cota - 0.50);

            if (transbordando || (quaseTransbordando && prob >= 0.30) || (mare >= 3.00 && prob >= 0.50) || prob >= 0.85 || mare >= 3.50) {
                return NivelRisco.ALTO;
            }
            if (cotaAlerta || mare >= 2.50 || prob >= 0.40) {
                return NivelRisco.MEDIO;
            }
            return NivelRisco.BAIXO;
        }

        if (prob >= 0.85) {
            return NivelRisco.ALTO;
        }
        if (prob >= 0.40) {
            return NivelRisco.MEDIO;
        }
        return NivelRisco.BAIXO;
    }

    private String gerarDescricaoCenario(NivelRisco risco, Double probChuva, BigDecimal nivelMare, Bairro bairro) {
        String nomeLocal = bairro != null ? bairro.getDescricao() : "Belém";
        double mare = nivelMare != null ? nivelMare.doubleValue() : 0.0;
        double prob = probChuva != null ? probChuva * 100 : 0.0;
        double cota = (bairro != null && bairro.getCotaTransbordamentoMetros() != null) ? bairro.getCotaTransbordamentoMetros() : 3.00;

        if (risco == NivelRisco.ALTO) {
            if (mare >= cota && cota < 6.0) {
                return String.format(Locale.US, "ALERTA CRÍTICO: Nível da maré (%.2fm) atingiu a cota de transbordamento de %s (%.2fm). Risco iminente de alagamento.",
                        mare, nomeLocal, cota);
            }
            return String.format(Locale.US, "ALERTA CRÍTICO: Combinação de maré elevada (%.2fm) com alta probabilidade de chuva (%.0f%%).",
                    mare, prob);
        } else if (risco == NivelRisco.MEDIO) {
            return String.format(Locale.US, "ATENÇÃO: Nível da maré em %.2fm e probabilidade de chuva em %.0f%%.",
                    mare, prob);
        }
        return "Condições normais. Sem risco significativo de alagamento.";
    }
}
