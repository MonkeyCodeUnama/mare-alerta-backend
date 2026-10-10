package com.marealerta.backend.alerta.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

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

import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.model.EnvioAlertaWhatsapp;
import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.alerta.model.OrigemAlerta;
import com.marealerta.backend.alerta.model.StatusEnvioWhatsapp;
import com.marealerta.backend.alerta.model.TipoAlerta;
import com.marealerta.backend.alerta.service.AlertaService;
import com.marealerta.backend.estabelecimento.model.Bairro;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertaControllerTest {

    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaController alertaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertaController).build();
    }

    @Test
    @DisplayName("GET /alertas deve retornar lista de alertas")
    void deveListarAlertasComSucesso() throws Exception {
        AlertaResponse response = AlertaResponse.builder()
                .id(1L)
                .estabelecimentoId(10L)
                .estabelecimentoNome("Mercado Campina")
                .nivelRisco(NivelRisco.MEDIO)
                .mensagem("Maré subindo rapidamente")
                .tipoAlerta(TipoAlerta.ALAGAMENTO)
                .origemAlerta(OrigemAlerta.AUTOMATICO)
                .dataEnvio(LocalDateTime.now())
                .build();

        when(alertaService.listarTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/alertas").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].estabelecimentoNome").value("Mercado Campina"))
                .andExpect(jsonPath("$[0].nivelRisco").value("MEDIO"));
    }

    @Test
    @DisplayName("POST /alertas deve criar alerta e retornar 201 Created")
    void deveCriarAlertaComSucesso() throws Exception {
        AlertaResponse response = AlertaResponse.builder()
                .id(2L)
                .estabelecimentoId(10L)
                .estabelecimentoNome("Mercado Campina")
                .nivelRisco(NivelRisco.ALTO)
                .mensagem("Risco crítico de alagamento")
                .tipoAlerta(TipoAlerta.ALAGAMENTO)
                .origemAlerta(OrigemAlerta.AUTOMATICO)
                .dataEnvio(LocalDateTime.now())
                .build();

        when(alertaService.criarAlerta(any(AlertaRequest.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "estabelecimentoId": 10,
                    "nivelRisco": "ALTO",
                    "mensagem": "Risco crítico de alagamento",
                    "tipoAlerta": "ALAGAMENTO",
                    "origemAlerta": "AUTOMATICO"
                }
                """;

        mockMvc.perform(post("/alertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.nivelRisco").value("ALTO"))
                .andExpect(jsonPath("$.mensagem").value("Risco crítico de alagamento"));
    }

    @Test
    @DisplayName("POST /alertas/bairro deve disparar alertas para bairro e retornar 201 Created")
    void deveDispararAlertaParaBairro() throws Exception {
        AlertaResponse response = AlertaResponse.builder()
                .id(3L)
                .estabelecimentoId(10L)
                .estabelecimentoNome("Mercado Campina")
                .nivelRisco(NivelRisco.ALTO)
                .mensagem("Alerta Defesa Civil")
                .tipoAlerta(TipoAlerta.PREVENTIVO)
                .build();

        when(alertaService.dispararAlertaBairro(eq(Bairro.CAMPINA), eq(NivelRisco.ALTO), any(), eq(TipoAlerta.PREVENTIVO), any()))
                .thenReturn(List.of(response));

        String jsonPayload = """
                {
                    "bairro": "Campina",
                    "nivelRisco": "ALTO",
                    "mensagem": "Alerta Defesa Civil",
                    "tipoAlerta": "PREVENTIVO",
                    "origemAlerta": "MANUAL"
                }
                """;

        mockMvc.perform(post("/alertas/bairro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(3L));
    }

    @Test
    @DisplayName("GET /alertas/{id} deve retornar detalhe do alerta")
    void deveObterDetalheAlerta() throws Exception {
        AlertaResponse response = AlertaResponse.builder()
                .id(5L)
                .estabelecimentoId(10L)
                .nivelRisco(NivelRisco.BAIXO)
                .mensagem("Sem risco")
                .tipoAlerta(TipoAlerta.ALAGAMENTO)
                .build();

        when(alertaService.buscarPorId(5L)).thenReturn(response);

        mockMvc.perform(get("/alertas/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.nivelRisco").value("BAIXO"));
    }

    @Test
    @DisplayName("GET /alertas/{id}/envios-whatsapp deve listar despachos do alerta")
    void deveListarEnviosWhatsappDoAlerta() throws Exception {
        EnvioAlertaWhatsapp envio = EnvioAlertaWhatsapp.builder()
                .id(1L)
                .telefoneDestinatario("5591987654321")
                .statusEnvio(StatusEnvioWhatsapp.ENVIADO)
                .whatsappMessageId("MSG-999")
                .tentativas(1)
                .criadoEm(Instant.now())
                .enviadoEm(Instant.now())
                .build();

        when(alertaService.listarEnviosWhatsapp(5L)).thenReturn(List.of(envio));

        mockMvc.perform(get("/alertas/5/envios-whatsapp").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].statusEnvio").value("ENVIADO"))
                .andExpect(jsonPath("$[0].whatsappMessageId").value("MSG-999"));
    }
}
