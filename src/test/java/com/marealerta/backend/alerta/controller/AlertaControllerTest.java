package com.marealerta.backend.alerta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marealerta.backend.alerta.AlertaService;
import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.alerta.model.TipoAlerta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertaController.class)
class AlertaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertaService alertaService;

    @Test
    void deveRetornarCriadoQuandoRequestForValido() throws Exception {
        AlertaRequest request = new AlertaRequest();
        request.setEstabelecimentoId(1L);
        request.setNivelRisco(NivelRisco.ALTO);
        request.setMensagem("Teste de alerta");
        request.setTipoAlerta(TipoAlerta.MARE_ALTA);

        AlertaResponse response = AlertaResponse.builder()
                .id(1L)
                .estabelecimentoId(1L)
                .nivelRisco(NivelRisco.ALTO)
                .mensagem("Teste de alerta")
                .build();

        when(alertaService.enviarAlerta(any(AlertaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/alertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}