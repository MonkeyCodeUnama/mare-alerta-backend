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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiscoServiceTest {

    @Mock
    private MlApiCliente mlApiClient;

    @Mock
    private PrevisaoChuvaMlRepository previsaoChuvaMlRepository;

    @Mock
    private TelemetriaService telemetriaService;

    @Mock
    private OpenMeteoClient openMeteoClient;

    @InjectMocks
    private RiscoService riscoService;

    private TelemetriaDashboardResponse telemetriaPadrao;
    private OpenMeteoClient.GfsFeaturesSnapshot gfsSnapshotPadrao;

    @BeforeEach
    void setUp() {
        telemetriaPadrao = TelemetriaDashboardResponse.builder()
                .estacaoCodigo("BELEM-CENTRO")
                .estacaoNome("Estação Ponto de Referência Belém")
                .nivelAtual(BigDecimal.valueOf(2.20))
                .chuvaAtual(BigDecimal.valueOf(0.0))
                .temperatura(BigDecimal.valueOf(29.0))
                .ventoVelocidade(BigDecimal.valueOf(12.5))
                .ventoDirecao(BigDecimal.valueOf(90.0))
                .build();

        Map<String, BigDecimal> features = Map.of(
                "temperature_2m", BigDecimal.valueOf(28.0),
                "relative_humidity_2m", BigDecimal.valueOf(80.0),
                "dew_point_2m", BigDecimal.valueOf(24.0),
                "pressure_msl", BigDecimal.valueOf(1012.0),
                "surface_pressure", BigDecimal.valueOf(1010.0),
                "cloud_cover", BigDecimal.valueOf(50.0),
                "cloud_cover_low", BigDecimal.valueOf(20.0),
                "cloud_cover_mid", BigDecimal.valueOf(10.0),
                "cloud_cover_high", BigDecimal.valueOf(30.0),
                "wind_speed_10m", BigDecimal.valueOf(14.0)
        );
        Map<String, String> units = Map.of("temperature_2m", "°C", "precipitation", "mm");

        gfsSnapshotPadrao = new OpenMeteoClient.GfsFeaturesSnapshot("2026-10-22T21:00:00Z", features, units);
    }

    @Test
    @DisplayName("Deve calcular risco ALTO quando maré >= 3.0m e probabilidade de chuva >= 50%")
    void deveCalcularRiscoAltoParaMareElevadaEChuva() {
        TelemetriaDashboardResponse telemetriaCritica = TelemetriaDashboardResponse.builder()
                .estacaoCodigo("BELEM-CENTRO")
                .nivelAtual(BigDecimal.valueOf(3.10))
                .chuvaAtual(BigDecimal.valueOf(25.0))
                .build();

        Instant agora = Instant.now();
        Instant alvo = agora.plus(1, ChronoUnit.HOURS);

        PrevisaoMlResponse mlResponse = PrevisaoMlResponse.builder()
                .modelVersion("v1.0.0")
                .issuedAt(agora)
                .targetTime(alvo)
                .horizonHours(1)
                .rainProbability(0.65)
                .rainExpected(true)
                .expectedPrecipitationMm(BigDecimal.valueOf(35.0))
                .qualityStatus(StatusQualidadeMl.OK)
                .build();

        when(telemetriaService.obterDashboard()).thenReturn(telemetriaCritica);
        when(openMeteoClient.buscarFeaturesGfsAtuais(anyDouble(), anyDouble())).thenReturn(gfsSnapshotPadrao);
        when(mlApiClient.solicitarPrevisao(any(PrevisaoMlRequest.class))).thenReturn(mlResponse);

        RiscoResponse response = riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO");

        assertThat(response).isNotNull();
        assertThat(response.getNivelRisco()).isEqualTo(NivelRisco.ALTO);
        assertThat(response.getAlertaEmitido()).isTrue();
        assertThat(response.getProbabilidadeChuva()).isEqualTo(0.65);
        assertThat(response.getNivelMarePrevistoMetros()).isEqualByComparingTo(BigDecimal.valueOf(3.10));
        assertThat(response.getDescricaoCenario()).contains("ALERTA CRÍTICO");
        assertThat(response.getHorarioPicoPrevisto()).isNotNull();

        ArgumentCaptor<PrevisaoChuvaMl> captor = ArgumentCaptor.forClass(PrevisaoChuvaMl.class);
        verify(previsaoChuvaMlRepository).save(captor.capture());
        PrevisaoChuvaMl salva = captor.getValue();
        assertThat(salva.getEstacaoCodigo()).isEqualTo("BELEM_CENTRO");
        assertThat(salva.getProbabilidadeChuva()).isEqualTo(0.65);
        assertThat(salva.getChuvaEsperada()).isTrue();
        assertThat(salva.getStatusQualidade()).isEqualTo(StatusQualidadeMl.OK);
    }

    @Test
    @DisplayName("Deve calcular risco ALTO quando probabilidade de chuva >= 85% mesmo com maré baixa")
    void deveCalcularRiscoAltoParaChuvaExtrema() {
        Instant agora = Instant.now();
        PrevisaoMlResponse mlResponse = PrevisaoMlResponse.builder()
                .modelVersion("v1.0.0")
                .issuedAt(agora)
                .targetTime(agora.plus(1, ChronoUnit.HOURS))
                .horizonHours(1)
                .rainProbability(0.90)
                .rainExpected(true)
                .expectedPrecipitationMm(BigDecimal.valueOf(45.0))
                .qualityStatus(StatusQualidadeMl.OK)
                .build();

        when(telemetriaService.obterDashboard()).thenReturn(telemetriaPadrao);
        when(openMeteoClient.buscarFeaturesGfsAtuais(anyDouble(), anyDouble())).thenReturn(gfsSnapshotPadrao);
        when(mlApiClient.solicitarPrevisao(any(PrevisaoMlRequest.class))).thenReturn(mlResponse);

        RiscoResponse response = riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO");

        assertThat(response.getNivelRisco()).isEqualTo(NivelRisco.ALTO);
        assertThat(response.getAlertaEmitido()).isTrue();
    }

    @Test
    @DisplayName("Deve calcular risco MEDIO quando probabilidade de chuva for intermediária (>= 40%)")
    void deveCalcularRiscoMedioParaProbabilidadeIntermediaria() {
        Instant agora = Instant.now();
        PrevisaoMlResponse mlResponse = PrevisaoMlResponse.builder()
                .modelVersion("v1.0.0")
                .issuedAt(agora)
                .targetTime(agora.plus(1, ChronoUnit.HOURS))
                .horizonHours(1)
                .rainProbability(0.45)
                .rainExpected(false)
                .expectedPrecipitationMm(BigDecimal.valueOf(5.0))
                .qualityStatus(StatusQualidadeMl.OK)
                .build();

        when(telemetriaService.obterDashboard()).thenReturn(telemetriaPadrao);
        when(openMeteoClient.buscarFeaturesGfsAtuais(anyDouble(), anyDouble())).thenReturn(gfsSnapshotPadrao);
        when(mlApiClient.solicitarPrevisao(any(PrevisaoMlRequest.class))).thenReturn(mlResponse);

        RiscoResponse response = riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO");

        assertThat(response.getNivelRisco()).isEqualTo(NivelRisco.MEDIO);
        assertThat(response.getAlertaEmitido()).isFalse();
        assertThat(response.getDescricaoCenario()).contains("ATENÇÃO");
    }

    @Test
    @DisplayName("Deve calcular risco BAIXO quando maré e chuva estiverem em níveis seguros")
    void deveCalcularRiscoBaixoParaCondicoesNormais() {
        Instant agora = Instant.now();
        PrevisaoMlResponse mlResponse = PrevisaoMlResponse.builder()
                .modelVersion("v1.0.0")
                .issuedAt(agora)
                .targetTime(agora.plus(1, ChronoUnit.HOURS))
                .horizonHours(1)
                .rainProbability(0.15)
                .rainExpected(false)
                .expectedPrecipitationMm(BigDecimal.ZERO)
                .qualityStatus(StatusQualidadeMl.OK)
                .build();

        when(telemetriaService.obterDashboard()).thenReturn(telemetriaPadrao);
        when(openMeteoClient.buscarFeaturesGfsAtuais(anyDouble(), anyDouble())).thenReturn(gfsSnapshotPadrao);
        when(mlApiClient.solicitarPrevisao(any(PrevisaoMlRequest.class))).thenReturn(mlResponse);

        RiscoResponse response = riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO");

        assertThat(response.getNivelRisco()).isEqualTo(NivelRisco.BAIXO);
        assertThat(response.getAlertaEmitido()).isFalse();
        assertThat(response.getDescricaoCenario()).contains("Condições normais");
    }

    @Test
    @DisplayName("Deve propagar excecao quando servico de ML falhar sem criar dados mockados")
    void devePropagarExcecaoQuandoMlFalhar() {
        when(telemetriaService.obterDashboard()).thenReturn(telemetriaPadrao);
        when(openMeteoClient.buscarFeaturesGfsAtuais(anyDouble(), anyDouble())).thenReturn(gfsSnapshotPadrao);
        when(mlApiClient.solicitarPrevisao(any(PrevisaoMlRequest.class)))
                .thenThrow(new RuntimeException("Connection refused - FastAPI offline"));

        assertThatThrownBy(() -> riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Connection refused");

        verify(previsaoChuvaMlRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve enviar request de predicao com dados reais extraidos da Open-Meteo GFS")
    void deveEnviarRequestPredicaoComDadosReais() {
        when(telemetriaService.obterDashboard()).thenReturn(telemetriaPadrao);
        when(openMeteoClient.buscarFeaturesGfsAtuais(anyDouble(), anyDouble())).thenReturn(gfsSnapshotPadrao);
        when(mlApiClient.solicitarPrevisao(any(PrevisaoMlRequest.class))).thenReturn(
                PrevisaoMlResponse.builder()
                        .rainProbability(0.2)
                        .rainExpected(false)
                        .build()
        );

        riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO");

        ArgumentCaptor<PrevisaoMlRequest> captor = ArgumentCaptor.forClass(PrevisaoMlRequest.class);
        verify(mlApiClient).solicitarPrevisao(captor.capture());
        PrevisaoMlRequest sentRequest = captor.getValue();

        assertThat(sentRequest.featureTime()).isEqualTo("2026-10-22T21:00:00Z");
        assertThat(sentRequest.schemaVersion()).isEqualTo("rain-hourly-v1");
        assertThat(sentRequest.horizonHours()).isEqualTo(1);
        assertThat(sentRequest.features()).containsKey("temperature_2m");
    }
}
