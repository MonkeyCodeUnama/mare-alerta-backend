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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiscoServiceTest {

    @Mock
    private MlApiCliente mlApiClient;

    @Mock
    private PrevisaoChuvaMlRepository previsaoChuvaMlRepository;

    @Mock
    private TelemetriaService telemetriaService;

    @InjectMocks
    private RiscoService riscoService;

    private TelemetriaDashboardResponse telemetriaPadrao;

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
        when(mlApiClient.solicitarPrevisao(any(PrevisaoMlRequest.class))).thenReturn(mlResponse);

        RiscoResponse response = riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO");

        assertThat(response.getNivelRisco()).isEqualTo(NivelRisco.BAIXO);
        assertThat(response.getAlertaEmitido()).isFalse();
        assertThat(response.getDescricaoCenario()).contains("Condições normais");
    }
}

