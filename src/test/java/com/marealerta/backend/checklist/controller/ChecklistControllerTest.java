package com.marealerta.backend.checklist.controller;

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

import com.marealerta.backend.checklist.dto.ItemChecklistRequest;
import com.marealerta.backend.checklist.dto.ItemChecklistResponse;
import com.marealerta.backend.checklist.model.PrioridadeChecklist;
import com.marealerta.backend.checklist.model.StatusChecklist;
import com.marealerta.backend.checklist.service.ChecklistService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(checklistController).build();
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
    @DisplayName("DELETE /checklists/{id} deve retornar 204 No Content")
    void deveDeletarItem() throws Exception {
        mockMvc.perform(delete("/checklists/5"))
                .andExpect(status().isNoContent());

        verify(checklistService).deletar(5L);
    }
}
