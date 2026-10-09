package com.marealerta.backend.telemetria.controller;

import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}

