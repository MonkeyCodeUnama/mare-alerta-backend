package com.marealerta.backend.telemetria.service;

import com.marealerta.backend.telemetria.dto.LeituraTelemetriaRequest;
import com.marealerta.backend.telemetria.dto.LeituraTelemetriaResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaPontoGraficoDTO;
import com.marealerta.backend.telemetria.model.FonteTelemetria;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("Deve sincronizar dados meteorológicos e marinhos da API Open-Meteo")
    void deveSincronizarOpenMeteoComSucesso() {
        String horaAtualStr = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).toString();
        String horaProxStr = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0).toString();

        OpenMeteoClient.CurrentUnits current = new OpenMeteoClient.CurrentUnits(
                horaAtualStr,
                BigDecimal.valueOf(12.0),
                BigDecimal.valueOf(30.0),
                BigDecimal.valueOf(80.0),
                BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(1012.0),
                BigDecimal.valueOf(1013.0),
                BigDecimal.valueOf(70.0),
                BigDecimal.valueOf(18.0),
                BigDecimal.valueOf(80.0),
                BigDecimal.valueOf(25.0)
        );

        OpenMeteoClient.HourlyData hourly = new OpenMeteoClient.HourlyData(
                List.of(horaProxStr),
                List.of(BigDecimal.valueOf(2.0)),
                List.of(BigDecimal.valueOf(29.0)),
                List.of(BigDecimal.valueOf(15.0)),
                List.of(BigDecimal.valueOf(75.0))
        );

        OpenMeteoClient.OpenMeteoResponse apiResponse = new OpenMeteoClient.OpenMeteoResponse(current, hourly);

        OpenMeteoClient.MarineCurrent marineCurrent = new OpenMeteoClient.MarineCurrent(horaAtualStr, BigDecimal.valueOf(2.15));
        OpenMeteoClient.MarineHourly marineHourly = new OpenMeteoClient.MarineHourly(
                List.of(horaProxStr),
                List.of(BigDecimal.valueOf(2.45))
        );
        OpenMeteoClient.OpenMeteoMarineResponse marineResponse = new OpenMeteoClient.OpenMeteoMarineResponse(marineCurrent, marineHourly);

        when(openMeteoClient.buscarDadosAtuaisEPrevisao()).thenReturn(apiResponse);
        when(openMeteoClient.buscarDadosMarinhos(any(), any())).thenReturn(marineResponse);
        when(repository.findByEstacaoCodigoAndFonteAndTipoRegistroAndDataHora(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(repository.save(any(LeituraTelemetria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeituraTelemetriaResponse response = telemetriaService.sincronizarOpenMeteo();

        assertThat(response).isNotNull();
        assertThat(response.getNivelMare()).isEqualByComparingTo(BigDecimal.valueOf(2.15));
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
    @DisplayName("Deve retornar dados consolidados do dashboard com tendencia, preamar e campos formatados")
    void deveRetornarDashboardComSucesso() {
        LocalDateTime agora = LocalDateTime.now();
        leituraBase.setDataHora(agora);

        LeituraTelemetria previsaoProxima = LeituraTelemetria.builder()
                .estacaoCodigo("BELEM-CENTRO")
                .tipoRegistro(TipoRegistroTelemetria.PREVISAO)
                .nivelMare(BigDecimal.valueOf(2.70))
                .dataHora(agora.plusHours(1))
                .build();

        LeituraTelemetria previsaoPico = LeituraTelemetria.builder()
                .estacaoCodigo("BELEM-CENTRO")
                .tipoRegistro(TipoRegistroTelemetria.PREVISAO)
                .nivelMare(BigDecimal.valueOf(3.10))
                .dataHora(agora.plusHours(3))
                .build();

        when(repository.findTop1ByEstacaoCodigoAndTipoRegistroOrderByDataHoraDesc(
                "BELEM-CENTRO", TipoRegistroTelemetria.OBSERVADO))
                .thenReturn(Optional.of(leituraBase));

        when(repository.findByTipoRegistroAndDataHoraBetweenOrderByDataHoraAsc(
                eq(TipoRegistroTelemetria.PREVISAO), eq(agora), any()))
                .thenReturn(List.of(previsaoProxima, previsaoPico));

        TelemetriaDashboardResponse dashboard = telemetriaService.obterDashboard();

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getEstacaoCodigo()).isEqualTo("BELEM-CENTRO");
        assertThat(dashboard.getNivelAtual()).isEqualByComparingTo(BigDecimal.valueOf(2.40));
        assertThat(dashboard.getNivelFormatado()).isEqualTo("2.40 m");
        assertThat(dashboard.getChuvaAtual()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        assertThat(dashboard.getChuvaFormatada()).isEqualTo("5.0 mm/h");
        assertThat(dashboard.getTemperaturaFormatada()).isEqualTo("28.5 °C");
        assertThat(dashboard.getVentoFormatado()).isEqualTo("14.0 km/h");
        assertThat(dashboard.getNivelRisco()).isEqualTo("BAIXO");
        assertThat(dashboard.getTendencia()).isEqualTo("SUBINDO");
        assertThat(dashboard.getPreamarHorario()).isEqualTo(agora.plusHours(3));
        assertThat(dashboard.getPreamarHorarioFormatado()).isNotNull();
    }

    @Test
    @DisplayName("Deve resincronizar automaticamente quando leitura do dashboard tiver mais de 30 minutos")
    void deveResincronizarQuandoLeituraExpirada() {
        LocalDateTime antiga = LocalDateTime.now().minusMinutes(45);
        leituraBase.setDataHora(antiga);

        when(repository.findTop1ByEstacaoCodigoAndTipoRegistroOrderByDataHoraDesc(
                "BELEM-CENTRO", TipoRegistroTelemetria.OBSERVADO))
                .thenReturn(Optional.of(leituraBase));

        when(repository.findByTipoRegistroAndDataHoraBetweenOrderByDataHoraAsc(
                eq(TipoRegistroTelemetria.PREVISAO), any(), any()))
                .thenReturn(List.of());

        TelemetriaDashboardResponse dashboard = telemetriaService.obterDashboard();

        assertThat(dashboard).isNotNull();
        verify(openMeteoClient).buscarDadosAtuaisEPrevisao();
    }

    @Test
    @DisplayName("Deve calcular tendencia como DESCENDO quando proxima previsao for menor")
    void deveCalcularTendenciaDescendo() {
        LocalDateTime agora = LocalDateTime.now();
        leituraBase.setDataHora(agora);
        leituraBase.setNivelMare(BigDecimal.valueOf(2.80));

        LeituraTelemetria previsaoMenor = LeituraTelemetria.builder()
                .estacaoCodigo("BELEM-CENTRO")
                .tipoRegistro(TipoRegistroTelemetria.PREVISAO)
                .nivelMare(BigDecimal.valueOf(2.40))
                .dataHora(agora.plusHours(1))
                .build();

        when(repository.findTop1ByEstacaoCodigoAndTipoRegistroOrderByDataHoraDesc(
                "BELEM-CENTRO", TipoRegistroTelemetria.OBSERVADO))
                .thenReturn(Optional.of(leituraBase));

        when(repository.findByTipoRegistroAndDataHoraBetweenOrderByDataHoraAsc(
                eq(TipoRegistroTelemetria.PREVISAO), eq(agora), any()))
                .thenReturn(List.of(previsaoMenor));

        TelemetriaDashboardResponse dashboard = telemetriaService.obterDashboard();

        assertThat(dashboard.getTendencia()).isEqualTo("DESCENDO");
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

    @Test
    @DisplayName("Deve obter histórico recente de leituras")
    void deveObterHistoricoRecenteComSucesso() {
        when(repository.findTop10ByEstacaoCodigoOrderByDataHoraDesc("BELEM-CENTRO"))
                .thenReturn(List.of(leituraBase));

        List<LeituraTelemetriaResponse> historico = telemetriaService.obterHistoricoRecente("BELEM-CENTRO");

        assertThat(historico).hasSize(1);
        assertThat(historico.get(0).getEstacaoCodigo()).isEqualTo("BELEM-CENTRO");
        verify(repository).findTop10ByEstacaoCodigoOrderByDataHoraDesc("BELEM-CENTRO");
    }
}
