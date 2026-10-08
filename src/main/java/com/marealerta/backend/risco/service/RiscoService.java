package com.marealerta.backend.risco.service;

import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.risco.dto.PrevisaoMlRequest;
import com.marealerta.backend.risco.dto.PrevisaoMlResponse;
import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.risco.model.PrevisaoChuvaMl;
import com.marealerta.backend.risco.model.StatusQualidadeMl;
import com.marealerta.backend.risco.repository.PrevisaoChuvaMlRepository;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.service.TelemetriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RiscoService {

    private final MlApiCliente mlApiClient;
    private final PrevisaoChuvaMlRepository previsaoChuvaMlRepository;
    private final TelemetriaService telemetriaService;

    @Transactional
    public RiscoResponse processarEObterRiscoParaLocalidade(String localidade) {
        double lat = -1.4558;
        double lon = -48.5044;

        // 1. Obtém os dados da Telemetria
        TelemetriaDashboardResponse telemetria = telemetriaService.obterDashboard();

        // 2. Mapeamento com os getters exatos da classe TelemetriaDashboardResponse
        Map<String, BigDecimal> features = new HashMap<>();
        features.put("temperature_2m", telemetria.getTemperatura() != null ? telemetria.getTemperatura() : BigDecimal.valueOf(28.0));
        features.put("relative_humidity_2m", BigDecimal.valueOf(85.0)); // Valor default caso não haja sensor de humidade
        features.put("dew_point_2m", BigDecimal.valueOf(24.0));
        features.put("pressure_msl", BigDecimal.valueOf(1010.0));
        features.put("surface_pressure", BigDecimal.valueOf(1008.0));
        features.put("cloud_cover", BigDecimal.valueOf(75.0));
        features.put("cloud_cover_low", BigDecimal.valueOf(50.0));
        features.put("cloud_cover_mid", BigDecimal.ZERO);
        features.put("cloud_cover_high", BigDecimal.valueOf(50.0));
        features.put("wind_speed_10m", telemetria.getVentoVelocidade() != null ? telemetria.getVentoVelocidade() : BigDecimal.ZERO);
        features.put("wind_direction_10m", telemetria.getVentoDirecao() != null ? telemetria.getVentoDirecao() : BigDecimal.valueOf(360.0));
        features.put("wind_gusts_10m", telemetria.getVentoVelocidade() != null ? telemetria.getVentoVelocidade() : BigDecimal.ZERO);
        features.put("cape", BigDecimal.valueOf(500.0));
        features.put("relative_humidity_850hPa", BigDecimal.valueOf(85.0));
        features.put("relative_humidity_700hPa", BigDecimal.valueOf(50.0));
        features.put("relative_humidity_500hPa", BigDecimal.valueOf(40.0));
        features.put("temperature_850hPa", BigDecimal.valueOf(18.0));
        features.put("temperature_700hPa", BigDecimal.valueOf(10.0));
        features.put("geopotential_height_500hPa", BigDecimal.valueOf(5880.0));
        features.put("precipitation", telemetria.getChuvaAtual() != null ? telemetria.getChuvaAtual() : BigDecimal.ZERO);
        Map<String, String> units = Map.ofEntries(
                Map.entry("temperature_2m", "°C"),
                Map.entry("relative_humidity_2m", "%"),
                Map.entry("dew_point_2m", "°C"),
                Map.entry("pressure_msl", "hPa"),
                Map.entry("surface_pressure", "hPa"),
                Map.entry("cloud_cover", "%"),
                Map.entry("cloud_cover_low", "%"),
                Map.entry("cloud_cover_mid", "%"),
                Map.entry("cloud_cover_high", "%"),
                Map.entry("wind_speed_10m", "km/h"),
                Map.entry("wind_direction_10m", "°"),
                Map.entry("wind_gusts_10m", "km/h"),
                Map.entry("cape", "J/kg"),
                Map.entry("relative_humidity_850hPa", "%"),
                Map.entry("relative_humidity_700hPa", "%"),
                Map.entry("relative_humidity_500hPa", "%"),
                Map.entry("temperature_850hPa", "°C"),
                Map.entry("temperature_700hPa", "°C"),
                Map.entry("geopotential_height_500hPa", "m"),
                Map.entry("precipitation", "mm")
        );

        PrevisaoMlRequest request = new PrevisaoMlRequest(
                "rain-hourly-v1",
                BigDecimal.valueOf(lat),
                BigDecimal.valueOf(lon),
                OffsetDateTime.now().toString(),
                1,
                features,
                units
        );

        return processarEObterRisco(request, localidade, telemetria);
    }

    @Transactional
    public RiscoResponse processarEObterRisco(PrevisaoMlRequest request, String localidade, TelemetriaDashboardResponse telemetria) {
        if (telemetria == null) {
            telemetria = telemetriaService.obterDashboard();
        }

        PrevisaoMlResponse responseMl = mlApiClient.solicitarPrevisao(request);

        String estacao = (localidade != null && !localidade.isBlank()) ? localidade : "BELEM_CENTRO";
        Instant instanteEmissao = responseMl.getIssuedAt() != null ? responseMl.getIssuedAt() : Instant.now();
        Instant instanteAlvo = responseMl.getTargetTime() != null ? responseMl.getTargetTime() : Instant.now();
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