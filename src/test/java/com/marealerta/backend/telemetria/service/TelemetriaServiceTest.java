package com.marealerta.backend.telemetria.service;

import com.marealerta.backend.telemetria.dto.LeituraTelemetriaRequest;
import com.marealerta.backend.telemetria.dto.LeituraTelemetriaResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaPontoGraficoDTO;
import com.marealerta.backend.telemetria.model.LeituraTelemetria;
import com.marealerta.backend.telemetria.model.TipoRegistroTelemetria;
import com.marealerta.backend.telemetria.repository.LeituraTelemetriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetriaServiceTest {

    @Mock
    private LeituraTelemetriaRepository repository;

    @Mock
    private OpenMeteoClient openMeteoClient;

    @InjectMocks
    private TelemetriaService telemetriaService;

    private LeituraTelemetria leituraBase;

    @BeforeEach
    void setUp() {
        leituraBase = LeituraTelemetria.builder()
                .id(1L)
                .estacaoCodigo("BELEM-CENTRO")
                .estacaoNome("Estação Ponto de Referência Belém")
                .tipoRegistro(TipoRegistroTelemetria.OBSERVADO)
                .nivelMare(BigDecimal.valueOf(2.40))
                .precipitacao(BigDecimal.valueOf(5.0))
                .temperatura(BigDecimal.valueOf(28.5))
                .ventoVelocidade(BigDecimal.valueOf(14.0))
                .ventoDirecao(BigDecimal.valueOf(120.0))
                .dataHora(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve salvar leitura manual com sucesso")
    void deveSalvarLeituraManualComSucesso() {
        LeituraTelemetriaRequest request = LeituraTelemetriaRequest.builder()
                .estacaoCodigo("BELEM-CENTRO")
                .tipoRegistro(TipoRegistroTelemetria.OBSERVADO)
                .nivelMare(BigDecimal.valueOf(2.50))
                .precipitacao(BigDecimal.valueOf(10.0))
                .temperatura(BigDecimal.valueOf(27.0))
                .ventoVelocidade(BigDecimal.valueOf(15.0))
                .ventoDirecao(BigDecimal.valueOf(90.0))
                .dataHora(LocalDateTime.now())
                .build();

        when(repository.save(any(LeituraTelemetria.class))).thenAnswer(invocation -> {
            LeituraTelemetria l = invocation.getArgument(0);
            return LeituraTelemetria.builder()
                    .id(10L)
                    .estacaoCodigo(l.getEstacaoCodigo())
                    .estacaoNome(l.getEstacaoNome())
                    .tipoRegistro(l.getTipoRegistro())
                    .nivelMare(l.getNivelMare())
                    .precipitacao(l.getPrecipitacao())
                    .temperatura(l.getTemperatura())
                    .ventoVelocidade(l.getVentoVelocidade())
                    .ventoDirecao(l.getVentoDirecao())
                    .dataHora(l.getDataHora())
                    .build();
        });

        LeituraTelemetriaResponse response = telemetriaService.salvarManual(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTipoRegistro()).isEqualTo(TipoRegistroTelemetria.OBSERVADO);
        verify(repository).save(any(LeituraTelemetria.class));
    }

    @Test
    @DisplayName("Deve sincronizar dados meteorológicos da API Open-Meteo")
    void deveSincronizarOpenMeteoComSucesso() {
        OpenMeteoClient.CurrentUnits current = new OpenMeteoClient.CurrentUnits(
                LocalDateTime.now().toString(),
                BigDecimal.valueOf(12.0),
                BigDecimal.valueOf(30.0),
                BigDecimal.valueOf(18.0),
                BigDecimal.valueOf(80.0)
        );

        OpenMeteoClient.HourlyData hourly = new OpenMeteoClient.HourlyData(
                List.of(LocalDateTime.now().plusHours(1).toString()),
                List.of(BigDecimal.valueOf(2.0)),
                List.of(BigDecimal.valueOf(29.0)),
                List.of(BigDecimal.valueOf(15.0)),
                List.of(BigDecimal.valueOf(75.0))
        );

        OpenMeteoClient.OpenMeteoResponse apiResponse = new OpenMeteoClient.OpenMeteoResponse(current, hourly);

        when(openMeteoClient.buscarDadosAtuaisEPrevisao()).thenReturn(apiResponse);
        when(repository.save(any(LeituraTelemetria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeituraTelemetriaResponse response = telemetriaService.sincronizarOpenMeteo();

        assertThat(response).isNotNull();
        assertThat(response.getPrecipitacao()).isEqualByComparingTo(BigDecimal.valueOf(12.0));
        assertThat(response.getTemperatura()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
        verify(repository).save(any(LeituraTelemetria.class));
        verify(repository).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando OpenMeteo retornar resposta nula")
    void deveLancarExcecaoQuandoOpenMeteoRetornarNulo() {
        when(openMeteoClient.buscarDadosAtuaisEPrevisao()).thenReturn(null);

        assertThatThrownBy(() -> telemetriaService.sincronizarOpenMeteo())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao obter dados meteorológicos");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar dados consolidados do dashboard a partir da última leitura")
    void deveRetornarDashboardComSucesso() {
        when(repository.findTop1ByEstacaoCodigoAndTipoRegistroOrderByDataHoraDesc(
                "BELEM-CENTRO", TipoRegistroTelemetria.OBSERVADO))
                .thenReturn(Optional.of(leituraBase));

        TelemetriaDashboardResponse dashboard = telemetriaService.obterDashboard();

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getEstacaoCodigo()).isEqualTo("BELEM-CENTRO");
        assertThat(dashboard.getNivelAtual()).isEqualByComparingTo(BigDecimal.valueOf(2.40));
        assertThat(dashboard.getChuvaAtual()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        assertThat(dashboard.getNivelRisco()).isEqualTo("BAIXO");
        assertThat(dashboard.getTendencia()).isEqualTo("SUBINDO");
    }

    @Test
    @DisplayName("Deve gerar pontos de gráfico ordenados por dataHora")
    void deveObterPontosGrafico() {
        when(repository.findByEstacaoCodigoAndDataHoraBetweenOrderByDataHoraAsc(
                eq("BELEM-CENTRO"), any(), any()))
                .thenReturn(List.of(leituraBase));

        List<TelemetriaPontoGraficoDTO> pontos = telemetriaService.obterPontosGrafico();

        assertThat(pontos).hasSize(1);
        assertThat(pontos.get(0).getLevel()).isEqualByComparingTo(BigDecimal.valueOf(2.40));
        assertThat(pontos.get(0).getTime()).isNotNull();
    }
}

