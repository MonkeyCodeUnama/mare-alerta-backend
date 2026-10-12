package com.marealerta.backend.telemetria.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.marealerta.backend.telemetria.service.TelemetriaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.marealerta.backend.telemetria.dto.LeituraTelemetriaResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaPontoGraficoDTO;
import com.marealerta.backend.telemetria.model.TipoRegistroTelemetria;

@ExtendWith(MockitoExtension.class)
class TelemetriaControllerTest {

    @Mock
    private TelemetriaService telemetriaService;

    @InjectMocks
    private TelemetriaController telemetriaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(telemetriaController).build();
    }

    @Test
    @DisplayName("GET /telemetria/dashboard deve retornar 200 OK com métricas do dashboard")
    void deveObterDashboardComSucesso() throws Exception {
        TelemetriaDashboardResponse response = TelemetriaDashboardResponse.builder()
                .estacaoCodigo("BELEM-CENTRO")
                .estacaoNome("Estação Ponto de Referência Belém")
                .nivelAtual(BigDecimal.valueOf(2.80))
                .tendencia("SUBINDO")
                .preamarHorario(LocalDateTime.now().plusHours(2))
                .chuvaAtual(BigDecimal.valueOf(15.0))
                .temperatura(BigDecimal.valueOf(28.0))
                .ventoVelocidade(BigDecimal.valueOf(12.0))
                .ventoDirecao(BigDecimal.valueOf(180.0))
                .nivelRisco("MEDIO")
                .unidadeNivel("m")
                .unidadeChuva("mm/h")
                .build();

        when(telemetriaService.obterDashboard()).thenReturn(response);

        mockMvc.perform(get("/telemetria/dashboard")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estacaoCodigo").value("BELEM-CENTRO"))
                .andExpect(jsonPath("$.nivelAtual").value(2.80))
                .andExpect(jsonPath("$.chuvaAtual").value(15.0))
                .andExpect(jsonPath("$.nivelRisco").value("MEDIO"))
                .andExpect(jsonPath("$.tendencia").value("SUBINDO"));
    }

    @Test
    @DisplayName("GET /telemetria/grafico deve retornar 200 OK com pontos da série temporal")
    void deveObterPontosGraficoComSucesso() throws Exception {
        TelemetriaPontoGraficoDTO ponto = TelemetriaPontoGraficoDTO.builder()
                .time("12:00")
                .dataHora(LocalDateTime.now())
                .level(BigDecimal.valueOf(2.80))
                .rain(BigDecimal.valueOf(10.5))
                .temperature(BigDecimal.valueOf(29.0))
                .wind(BigDecimal.valueOf(14.0))
                .tipoRegistro("OBSERVADO")
                .build();

        when(telemetriaService.obterPontosGrafico()).thenReturn(List.of(ponto));

        mockMvc.perform(get("/telemetria/grafico")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].time").value("12:00"))
                .andExpect(jsonPath("$[0].level").value(2.80))
                .andExpect(jsonPath("$[0].rain").value(10.5))
                .andExpect(jsonPath("$[0].tipoRegistro").value("OBSERVADO"));
    }

    @Test
    @DisplayName("POST /telemetria/sincronizar deve retornar 200 OK com dados sincronizados")
    void deveSincronizarOpenMeteoComSucesso() throws Exception {
        LeituraTelemetriaResponse response = LeituraTelemetriaResponse.builder()
                .id(1L)
                .estacaoCodigo("BELEM-CENTRO")
                .estacaoNome("Estação Ponto de Referência Belém")
                .tipoRegistro(TipoRegistroTelemetria.OBSERVADO)
                .nivelMare(BigDecimal.valueOf(2.50))
                .precipitacao(BigDecimal.valueOf(5.0))
                .temperatura(BigDecimal.valueOf(28.0))
                .ventoVelocidade(BigDecimal.valueOf(12.0))
                .ventoDirecao(BigDecimal.valueOf(90.0))
                .dataHora(LocalDateTime.now())
                .build();

        when(telemetriaService.sincronizarOpenMeteo()).thenReturn(response);

        mockMvc.perform(post("/telemetria/sincronizar")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estacaoCodigo").value("BELEM-CENTRO"))
                .andExpect(jsonPath("$.temperatura").value(28.0))
                .andExpect(jsonPath("$.tipoRegistro").value("OBSERVADO"));
    }

    @Test
    @DisplayName("GET /telemetria/historico deve retornar 200 OK filtrado por estação")
    void deveObterHistoricoComFiltroComSucesso() throws Exception {
        LeituraTelemetriaResponse response = LeituraTelemetriaResponse.builder()
                .id(1L)
                .estacaoCodigo("BELEM-CENTRO")
                .estacaoNome("Estação Ponto de Referência Belém")
                .tipoRegistro(TipoRegistroTelemetria.OBSERVADO)
                .nivelMare(BigDecimal.valueOf(2.50))
                .dataHora(LocalDateTime.now())
                .build();

        when(telemetriaService.obterHistoricoRecente(eq("BELEM-CENTRO"))).thenReturn(List.of(response));

        mockMvc.perform(get("/telemetria/historico")
                        .param("estacaoCodigo", "BELEM-CENTRO")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estacaoCodigo").value("BELEM-CENTRO"));
    }

    @Test
    @DisplayName("GET /telemetria/historico sem parâmetro deve retornar 200 OK com estação padrão")
    void deveObterHistoricoSemFiltroComSucesso() throws Exception {
        LeituraTelemetriaResponse response = LeituraTelemetriaResponse.builder()
                .id(1L)
                .estacaoCodigo("BELEM-CENTRO")
                .estacaoNome("Estação Ponto de Referência Belém")
                .tipoRegistro(TipoRegistroTelemetria.OBSERVADO)
                .nivelMare(BigDecimal.valueOf(2.50))
                .dataHora(LocalDateTime.now())
                .build();

        when(telemetriaService.obterHistoricoRecente(isNull())).thenReturn(List.of(response));

        mockMvc.perform(get("/telemetria/historico")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estacaoCodigo").value("BELEM-CENTRO"));
    }
}
