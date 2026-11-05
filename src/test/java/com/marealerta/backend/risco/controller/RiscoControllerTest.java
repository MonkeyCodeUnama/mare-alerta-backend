package com.marealerta.backend.risco.controller;

import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.risco.service.RiscoService;
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
class RiscoControllerTest {

    @Mock
    private RiscoService riscoService;

    @InjectMocks
    private RiscoController riscoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(riscoController).build();
    }

    @Test
    @DisplayName("GET /risco/atual deve retornar 200 OK com nivel de risco e probabilidade")
    void deveConsultarRiscoAtualComSucesso() throws Exception {
        RiscoResponse response = RiscoResponse.builder()
                .bairro("BELEM_CENTRO")
                .nivelRisco(NivelRisco.MEDIO)
                .probabilidadeChuva(0.45)
                .probabilidadePercentual(45)
                .probabilidadeFormatada("45%")
                .nivelMarePrevistoMetros(BigDecimal.valueOf(2.50))
                .cotaFormatada("2.50 m")
                .cotaTransbordamentoMetros(2.80)
                .statusCota("ALERTA")
                .descricaoCenario("ATENÇÃO: Risco moderado.")
                .horarioPicoPrevisto(LocalDateTime.now().plusHours(1))
                .alertaEmitido(false)
                .build();

        when(riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO")).thenReturn(response);

        mockMvc.perform(get("/risco/atual")
                        .param("localidade", "BELEM_CENTRO")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bairro").value("BELEM_CENTRO"))
                .andExpect(jsonPath("$.nivelRisco").value("MEDIO"))
                .andExpect(jsonPath("$.probabilidadeChuva").value(0.45))
                .andExpect(jsonPath("$.probabilidadePercentual").value(45))
                .andExpect(jsonPath("$.probabilidadeFormatada").value("45%"))
                .andExpect(jsonPath("$.cotaFormatada").value("2.50 m"))
                .andExpect(jsonPath("$.statusCota").value("ALERTA"))
                .andExpect(jsonPath("$.alertaEmitido").value(false));
    }

    @Test
    @DisplayName("GET /risco/atual sem parametro localidade deve usar o valor default BELEM_CENTRO")
    void deveUsarLocalidadeDefault() throws Exception {
        RiscoResponse response = RiscoResponse.builder()
                .bairro("BELEM_CENTRO")
                .nivelRisco(NivelRisco.BAIXO)
                .probabilidadeChuva(0.10)
                .probabilidadePercentual(10)
                .probabilidadeFormatada("10%")
                .nivelMarePrevistoMetros(BigDecimal.valueOf(2.00))
                .cotaFormatada("2.00 m")
                .cotaTransbordamentoMetros(2.80)
                .statusCota("NORMAL")
                .descricaoCenario("Condições normais.")
                .horarioPicoPrevisto(LocalDateTime.now().plusHours(1))
                .alertaEmitido(false)
                .build();

        when(riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO")).thenReturn(response);

        mockMvc.perform(get("/risco/atual")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bairro").value("BELEM_CENTRO"))
                .andExpect(jsonPath("$.nivelRisco").value("BAIXO"))
                .andExpect(jsonPath("$.probabilidadeFormatada").value("10%"));
    }

    @Test
    @DisplayName("GET /risco/atual para localidade especifica com risco ALTO")
    void deveConsultarRiscoAltoParaLocalidadeEspecifica() throws Exception {
        RiscoResponse response = RiscoResponse.builder()
                .bairro("CIDADE_VELHA")
                .nivelRisco(NivelRisco.ALTO)
                .probabilidadeChuva(0.85)
                .probabilidadePercentual(85)
                .probabilidadeFormatada("85%")
                .nivelMarePrevistoMetros(BigDecimal.valueOf(3.20))
                .cotaFormatada("3.20 m")
                .cotaTransbordamentoMetros(2.70)
                .statusCota("TRANSBORDAMENTO")
                .descricaoCenario("ALERTA CRÍTICO: Risco iminente de alagamento.")
                .horarioPicoPrevisto(LocalDateTime.now().plusHours(1))
                .alertaEmitido(true)
                .build();

        when(riscoService.processarEObterRiscoParaLocalidade("CIDADE_VELHA")).thenReturn(response);

        mockMvc.perform(get("/risco/atual")
                        .param("localidade", "CIDADE_VELHA")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bairro").value("CIDADE_VELHA"))
                .andExpect(jsonPath("$.nivelRisco").value("ALTO"))
                .andExpect(jsonPath("$.statusCota").value("TRANSBORDAMENTO"))
                .andExpect(jsonPath("$.alertaEmitido").value(true));
    }
}
