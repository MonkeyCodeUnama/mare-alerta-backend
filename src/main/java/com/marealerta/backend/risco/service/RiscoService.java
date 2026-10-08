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
import java.util.Map;

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
        double lon = -48.4902;

        // 1. Busca os dados crus da Open-Meteo
        OpenMeteoClient.DadosMeteo dadosMeteo = openMeteoClient.buscarDadosAtuais(lat, lon, localidade);

        // 2. Mapa de features corrigido para snake_case
        Map<String, BigDecimal> features = Map.of(
                "temperatura_2m", BigDecimal.valueOf(dadosMeteo.temperatura()),
                "umidade_relativa_2m", BigDecimal.valueOf(dadosMeteo.umidade()),
                "pressao_superficie", BigDecimal.valueOf(dadosMeteo.pressao()),
                "cobertura_nuvens", BigDecimal.valueOf(dadosMeteo.nuvens()),
                "velocidade_vento_10m", BigDecimal.valueOf(dadosMeteo.ventoVelocidade()),
                "direcao_vento_10m", BigDecimal.valueOf(dadosMeteo.ventoDirecao()),
                "precipitacao_observada_1h", BigDecimal.valueOf(dadosMeteo.precipitacao()),
                "cape", BigDecimal.valueOf(dadosMeteo.cape())
        );

        // 3. Mapa de unidades corrigido para snake_case
        Map<String, String> units = Map.of(
                "temperatura_2m", "°C",
                "umidade_relativa_2m", "%",
                "pressao_superficie", "hPa",
                "cobertura_nuvens", "%",
                "velocidade_vento_10m", "km/h",
                "direcao_vento_10m", "°",
                "precipitacao_observada_1h", "mm",
                "cape", "J/kg"
        );

        // 4. Cria a requisição estruturada
        PrevisaoMlRequest request = new PrevisaoMlRequest(
                "rain-hourly-v1",
                BigDecimal.valueOf(lat),
                BigDecimal.valueOf(lon),
                OffsetDateTime.now().toString(),
                1,
                features,
                units
        );

        return processarEObterRisco(request, localidade);
    }

    @Transactional
    public RiscoResponse processarEObterRisco(PrevisaoMlRequest request, String localidade) {
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