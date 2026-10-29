package com.marealerta.backend.estabelecimento.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.marealerta.backend.estabelecimento.dto.EstabelecimentoAtualizacaoRequest;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import com.marealerta.backend.estabelecimento.service.EstabelecimentoService;

@ExtendWith(MockitoExtension.class)
class EstabelecimentoControllerTest {

    @Mock
    private EstabelecimentoService estabelecimentoService;

    @InjectMocks
    private EstabelecimentoController estabelecimentoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(estabelecimentoController).build();
    }

    @Test
    void deveBuscarEstabelecimentoPorId() throws Exception {
        EstabelecimentoResponse response = EstabelecimentoResponse.builder()
                .id(1L)
                .nome("Mercado Doca")
                .responsavel("Joao")
                .bairro(Bairro.REDUTO)
                .tipoNegocio(TipoNegocio.MERCADO)
                .build();

        when(estabelecimentoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/estabelecimento/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Mercado Doca"))
                .andExpect(jsonPath("$.bairro").value("REDUTO"));

        verify(estabelecimentoService).buscarPorId(1L);
    }

    @Test
    void deveBuscarMeuEstabelecimentoComPrincipal() throws Exception {
        Principal principal = () -> "usuario@doca.com";

        EstabelecimentoResponse response = EstabelecimentoResponse.builder()
                .id(5L)
                .nome("Mercado Doca")
                .responsavel("Joao")
                .build();

        when(estabelecimentoService.buscarPorEmailUsuario("usuario@doca.com")).thenReturn(response);

        mockMvc.perform(get("/estabelecimento/me").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.nome").value("Mercado Doca"));

        verify(estabelecimentoService).buscarPorEmailUsuario("usuario@doca.com");
    }

    @Test
    void deveRetornarUnauthorizedQuandoPrincipalNuloEmMe() throws Exception {
        mockMvc.perform(get("/estabelecimento/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveListarTodosOsEstabelecimentos() throws Exception {
        EstabelecimentoResponse r1 = EstabelecimentoResponse.builder().id(1L).nome("Estab 1").build();
        EstabelecimentoResponse r2 = EstabelecimentoResponse.builder().id(2L).nome("Estab 2").build();

        when(estabelecimentoService.listarTodos()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/estabelecimento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Estab 1"))
                .andExpect(jsonPath("$[1].nome").value("Estab 2"));

        mockMvc.perform(get("/estabelecimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(estabelecimentoService, org.mockito.Mockito.times(2)).listarTodos();
    }

    @Test
    void deveAtualizarEstabelecimento() throws Exception {
        EstabelecimentoResponse response = EstabelecimentoResponse.builder()
                .id(1L)
                .nome("Mercado Doca Atualizado")
                .responsavel("Joao Silva")
                .bairro(Bairro.REDUTO)
                .build();

        when(estabelecimentoService.atualizar(eq(1L), any(EstabelecimentoAtualizacaoRequest.class))).thenReturn(response);

        String payload = """
                {
                    "name": "Mercado Doca Atualizado",
                    "owner": "Joao Silva",
                    "neighborhood": "Reduto",
                    "type": "Mercado"
                }
                """;

        mockMvc.perform(put("/estabelecimento/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Mercado Doca Atualizado"));

        verify(estabelecimentoService).atualizar(eq(1L), any(EstabelecimentoAtualizacaoRequest.class));
    }
}
