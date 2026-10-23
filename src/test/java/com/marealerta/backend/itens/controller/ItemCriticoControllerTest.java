package com.marealerta.backend.itens.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marealerta.backend.itens.dto.ItemCriticoRequest;
import com.marealerta.backend.itens.dto.ItemCriticoResponse;
import com.marealerta.backend.itens.service.ItemCriticoService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemCriticoControllerTest {

    @Mock
    private ItemCriticoService itemCriticoService;

    @InjectMocks
    private ItemCriticoController itemCriticoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemCriticoController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve registrar item critico")
    void deveRegistrarItemCritico() throws Exception {
        ItemCriticoRequest request = ItemCriticoRequest.builder()
                .estabelecimentoId(1L)
                .nome("Freezer")
                .descricao("No chão")
                .build();

        ItemCriticoResponse response = ItemCriticoResponse.builder()
                .id(10L)
                .estabelecimentoId(1L)
                .nome("Freezer")
                .descricao("No chão")
                .build();

        when(itemCriticoService.criar(any(ItemCriticoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/itemcritico/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nome").value("Freezer"));
    }

    @Test
    @DisplayName("Deve listar itens criticos de um estabelecimento")
    void deveListarItensCriticos() throws Exception {
        ItemCriticoResponse response = ItemCriticoResponse.builder()
                .id(10L)
                .estabelecimentoId(1L)
                .nome("Freezer")
                .descricao("No chão")
                .build();

        when(itemCriticoService.listar(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/itemcritico/listar").param("estabelecimentoId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].nome").value("Freezer"));
    }

    @Test
    @DisplayName("Deve buscar item critico por ID")
    void deveBuscarPorId() throws Exception {
        ItemCriticoResponse response = ItemCriticoResponse.builder()
                .id(10L)
                .estabelecimentoId(1L)
                .nome("Freezer")
                .descricao("No chão")
                .build();

        when(itemCriticoService.buscarPorId(10L)).thenReturn(response);

        mockMvc.perform(get("/itemcritico/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nome").value("Freezer"));
    }

    @Test
    @DisplayName("Deve atualizar item critico")
    void deveAtualizarItemCritico() throws Exception {
        ItemCriticoRequest request = ItemCriticoRequest.builder()
                .nome("Freezer Elevado")
                .descricao("Elevado")
                .build();

        ItemCriticoResponse response = ItemCriticoResponse.builder()
                .id(10L)
                .estabelecimentoId(1L)
                .nome("Freezer Elevado")
                .descricao("Elevado")
                .build();

        when(itemCriticoService.atualizar(eq(10L), any(ItemCriticoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/itemcritico/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nome").value("Freezer Elevado"));
    }

    @Test
    @DisplayName("Deve deletar item critico")
    void deveDeletarItemCritico() throws Exception {
        doNothing().when(itemCriticoService).deletar(10L);

        mockMvc.perform(delete("/itemcritico/10"))
                .andExpect(status().isNoContent());
    }
}
