package com.marealerta.backend.risco.controller;

import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.alerta.service.AlertaService;
import com.marealerta.backend.checklist.dto.ChecklistResumoDTO;
import com.marealerta.backend.checklist.service.ChecklistService;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.service.EstabelecimentoService;
import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.risco.service.RiscoService;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.service.TelemetriaService;
import com.marealerta.backend.usuario.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PainelControllerTest {

    @Mock
    private RiscoService riscoService;

    @Mock
    private TelemetriaService telemetriaService;

    @Mock
    private EstabelecimentoService estabelecimentoService;

    @Mock
    private ChecklistService checklistService;

    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private PainelController painelController;

    private MockMvc mockMvc;
    private Usuario usuarioAutenticado;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = null;
        HandlerMethodArgumentResolver authResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return usuarioAutenticado;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(painelController)
                .setCustomArgumentResolvers(authResolver)
                .build();
    }

    @Test
    @DisplayName("GET /painel/resumo autenticado deve consolidar comercio, risco, telemetria, checklist e ultimo alerta")
    void deveRetornarResumoAutenticadoConsolidado() throws Exception {
        Usuario user = new Usuario();
        user.setEmail("comercio@belem.br");
        usuarioAutenticado = user;

        EstabelecimentoResponse estab = EstabelecimentoResponse.builder()
                .id(10L)
                .nome("Empório Ver-o-Peso")
                .bairro(Bairro.CAMPINA)
                .build();
        TelemetriaDashboardResponse dashboard = TelemetriaDashboardResponse.builder()
                .estacaoCodigo("BELEM-CENTRO")
                .nivelAtual(BigDecimal.valueOf(2.40))
                .build();
        RiscoResponse risco = RiscoResponse.builder()
                .bairro("CAMPINA")
                .nivelRisco(NivelRisco.CRITICO)
                .probabilidadePercentual(80)
                .vaiAlagar(true)
                .vaiChover(true)
                .mensagemCurta("Risco de alagamento em Campina.")
                .build();
        ChecklistResumoDTO checklist = ChecklistResumoDTO.builder()
                .totalItens(8)
                .concluidos(4)
                .pendentes(4)
                .percentualConcluido(50)
                .progressoFormatado("50%")
                .prontoParaInundacao(false)
                .build();
        AlertaResponse alerta = AlertaResponse.builder()
                .id(99L)
                .nivelRisco(NivelRisco.ALTO)
                .mensagem("Alerta emitido para Campina.")
                .build();

        when(estabelecimentoService.buscarPorEmailUsuario("comercio@belem.br")).thenReturn(estab);
        when(telemetriaService.obterDashboard()).thenReturn(dashboard);
        when(riscoService.processarEObterRiscoParaLocalidade("CAMPINA")).thenReturn(risco);
        when(checklistService.obterResumo(10L)).thenReturn(checklist);
        when(alertaService.listarPorEstabelecimento(10L)).thenReturn(List.of(alerta));

        mockMvc.perform(get("/painel/resumo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estabelecimento.nome").value("Empório Ver-o-Peso"))
                .andExpect(jsonPath("$.risco.vaiAlagar").value(true))
                .andExpect(jsonPath("$.risco.vaiChover").value(true))
                .andExpect(jsonPath("$.telemetriaDashboard.nivelAtual").value(2.40))
                .andExpect(jsonPath("$.checklistResumo.percentualConcluido").value(50))
                .andExpect(jsonPath("$.ultimoAlerta.id").value(99));
    }

    @Test
    @DisplayName("GET /painel/resumo anonimo deve retornar resumo publico com localidade padrao")
    void deveRetornarResumoAnonimoComLocalidadePadrao() throws Exception {
        TelemetriaDashboardResponse dashboard = TelemetriaDashboardResponse.builder()
                .estacaoCodigo("BELEM-CENTRO")
                .nivelAtual(BigDecimal.valueOf(1.80))
                .build();
        RiscoResponse risco = RiscoResponse.builder()
                .bairro("BELEM_CENTRO")
                .nivelRisco(NivelRisco.BAIXO)
                .vaiAlagar(false)
                .vaiChover(false)
                .build();
        ChecklistResumoDTO vazio = ChecklistResumoDTO.builder()
                .totalItens(0)
                .concluidos(0)
                .pendentes(0)
                .percentualConcluido(0)
                .progressoFormatado("0%")
                .prontoParaInundacao(false)
                .build();

        when(telemetriaService.obterDashboard()).thenReturn(dashboard);
        when(riscoService.processarEObterRiscoParaLocalidade("BELEM_CENTRO")).thenReturn(risco);
        when(checklistService.obterResumo(null)).thenReturn(vazio);
        when(alertaService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/painel/resumo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estabelecimento").doesNotExist())
                .andExpect(jsonPath("$.risco.bairro").value("BELEM_CENTRO"))
                .andExpect(jsonPath("$.risco.vaiAlagar").value(false))
                .andExpect(jsonPath("$.checklistResumo.totalItens").value(0));
    }
}
