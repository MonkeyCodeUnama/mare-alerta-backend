package com.marealerta.backend.checklist.controller;

import java.util.List;

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

import com.marealerta.backend.checklist.dto.ChecklistResumoDTO;
import com.marealerta.backend.checklist.dto.ItemChecklistRequest;
import com.marealerta.backend.checklist.dto.ItemChecklistResponse;
import com.marealerta.backend.checklist.model.PrioridadeChecklist;
import com.marealerta.backend.checklist.model.StatusChecklist;
import com.marealerta.backend.checklist.service.ChecklistService;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.usuario.model.Usuario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChecklistControllerTest {

    @Mock
    private ChecklistService checklistService;

    @InjectMocks
    private ChecklistController checklistController;

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

        mockMvc = MockMvcBuilders.standaloneSetup(checklistController)
                .setCustomArgumentResolvers(authResolver)
                .build();
    }

    @Test
    @DisplayName("POST /checklists deve criar item e retornar 201 Created")
    void deveCriarItemChecklist() throws Exception {
        ItemChecklistResponse response = ItemChecklistResponse.builder()
                .id(1L)
                .estabelecimentoId(10L)
                .titulo("Desligar disjuntor térreo")
                .status(StatusChecklist.PENDENTE)
                .prioridade(PrioridadeChecklist.ALTA)
                .build();

        when(checklistService.criar(any(ItemChecklistRequest.class))).thenReturn(response);

        String json = """
                {
                    "estabelecimentoId": 10,
                    "titulo": "Desligar disjuntor térreo",
                    "status": "PENDENTE",
                    "prioridade": "ALTA"
                }
                """;

        mockMvc.perform(post("/checklists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Desligar disjuntor térreo"));
    }

    @Test
    @DisplayName("GET /checklists deve listar itens do estabelecimento")
    void deveListarItensChecklist() throws Exception {
        ItemChecklistResponse response = ItemChecklistResponse.builder()
                .id(1L)
                .estabelecimentoId(10L)
                .titulo("Desligar disjuntor térreo")
                .build();

        when(checklistService.listar(eq(10L), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/checklists")
                        .param("estabelecimentoId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /checklists/resumo deve retornar métricas de progresso do checklist")
    void deveObterResumoChecklist() throws Exception {
        ChecklistResumoDTO resumo = ChecklistResumoDTO.builder()
                .totalItens(5L)
                .concluidos(3L)
                .pendentes(2L)
                .percentualConcluido(60)
                .progressoFormatado("60%")
                .prontoParaInundacao(false)
                .build();

        when(checklistService.obterResumo(10L)).thenReturn(resumo);

        mockMvc.perform(get("/checklists/resumo")
                        .param("estabelecimentoId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(5))
                .andExpect(jsonPath("$.concluidos").value(3))
                .andExpect(jsonPath("$.pendentes").value(2))
                .andExpect(jsonPath("$.percentualConcluido").value(60))
                .andExpect(jsonPath("$.progressoFormatado").value("60%"))
                .andExpect(jsonPath("$.prontoParaInundacao").value(false));
    }

    @Test
    @DisplayName("GET /checklists/resumo com usuario autenticado deve usar id do estabelecimento vinculado")
    void deveObterResumoComUsuarioAutenticado() throws Exception {
        Estabelecimento estab = Estabelecimento.builder()
                .id(15L)
                .build();
        Usuario user = new Usuario();
        user.setId(1L);
        user.setEstabelecimento(estab);
        usuarioAutenticado = user;

        ChecklistResumoDTO resumo = ChecklistResumoDTO.builder()
                .totalItens(4L)
                .concluidos(4L)
                .pendentes(0L)
                .percentualConcluido(100)
                .progressoFormatado("100%")
                .prontoParaInundacao(true)
                .build();

        when(checklistService.obterResumo(15L)).thenReturn(resumo);

        mockMvc.perform(get("/checklists/resumo")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentualConcluido").value(100))
                .andExpect(jsonPath("$.prontoParaInundacao").value(true));
    }

    @Test
    @DisplayName("GET /checklists/{id} deve retornar item por ID")
    void deveBuscarItemPorId() throws Exception {
        ItemChecklistResponse response = ItemChecklistResponse.builder()
                .id(5L)
                .titulo("Elevar estoque")
                .build();

        when(checklistService.buscarPorId(5L)).thenReturn(response);

        mockMvc.perform(get("/checklists/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.titulo").value("Elevar estoque"));
    }

    @Test
    @DisplayName("PUT /checklists/{id} deve atualizar item")
    void deveAtualizarItem() throws Exception {
        ItemChecklistResponse response = ItemChecklistResponse.builder()
                .id(5L)
                .titulo("Estoque elevado")
                .status(StatusChecklist.CONCLUIDO)
                .build();

        when(checklistService.atualizar(eq(5L), any(ItemChecklistRequest.class))).thenReturn(response);

        String json = """
                {
                    "estabelecimentoId": 10,
                    "titulo": "Estoque elevado",
                    "status": "CONCLUIDO"
                }
                """;

        mockMvc.perform(put("/checklists/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    @DisplayName("PATCH /checklists/{id}/toggle deve alternar status do item")
    void deveAlternarStatusItem() throws Exception {
        ItemChecklistResponse response = ItemChecklistResponse.builder()
                .id(5L)
                .titulo("Elevar freezers")
                .status(StatusChecklist.CONCLUIDO)
                .build();

        when(checklistService.alternarStatus(5L)).thenReturn(response);

        mockMvc.perform(patch("/checklists/5/toggle").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    @DisplayName("PATCH /checklists/{id}/concluir deve marcar item como concluido")
    void deveConcluirItem() throws Exception {
        ItemChecklistResponse response = ItemChecklistResponse.builder()
                .id(5L)
                .titulo("Elevar freezers")
                .status(StatusChecklist.CONCLUIDO)
                .build();

        when(checklistService.marcarComoConcluido(5L)).thenReturn(response);

        mockMvc.perform(patch("/checklists/5/concluir").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    @DisplayName("POST /checklists/gerar-padrao deve gerar lista padrao para o estabelecimento")
    void deveGerarChecklistPadrao() throws Exception {
        ItemChecklistResponse response = ItemChecklistResponse.builder()
                .id(1L)
                .titulo("Elevar freezers e refrigeradores")
                .build();

        when(checklistService.gerarItensPadrao(10L)).thenReturn(List.of(response));

        mockMvc.perform(post("/checklists/gerar-padrao")
                        .param("estabelecimentoId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].titulo").value("Elevar freezers e refrigeradores"));
    }

    @Test
    @DisplayName("DELETE /checklists/{id} deve retornar 204 No Content")
    void deveDeletarItem() throws Exception {
        mockMvc.perform(delete("/checklists/5"))
                .andExpect(status().isNoContent());

        verify(checklistService).deletar(5L);
    }
}
