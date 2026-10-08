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
    @DisplayName("GET /risco/atual deve retornar 200 OK com nível de risco e probabilidade")
    void deveConsultarRiscoAtualComSucesso() throws Exception {
        RiscoResponse response = RiscoResponse.builder()
                .bairro("BELEM_CENTRO")
                .nivelRisco(NivelRisco.MEDIO)
                .probabilidadeChuva(0.45)
                .nivelMarePrevistoMetros(BigDecimal.valueOf(2.50))
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
                .andExpect(jsonPath("$.alertaEmitido").value(false));
    }
}

