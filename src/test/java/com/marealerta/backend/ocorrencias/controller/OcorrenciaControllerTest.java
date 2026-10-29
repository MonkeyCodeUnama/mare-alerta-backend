package com.marealerta.backend.ocorrencias.controller;

import com.marealerta.backend.ocorrencias.dto.OcorrenciaFotoResponse;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaRequest;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaResponse;
import com.marealerta.backend.ocorrencias.service.OcorrenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OcorrenciaControllerTest {

    @Mock
    private OcorrenciaService ocorrenciaService;

    @InjectMocks
    private OcorrenciaController ocorrenciaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ocorrenciaController).build();
    }

    @Test
    @DisplayName("POST /ocorrencias deve retornar 201 Created com dados da ocorrência")
    void deveRegistrarOcorrenciaComSucesso() throws Exception {
        OcorrenciaResponse response = OcorrenciaResponse.builder()
                .id(1L)
                .protocolo("OC-10-1")
                .titulo("Alagamento Doca")
                .build();

        when(ocorrenciaService.registrarO(any(), any(OcorrenciaRequest.class))).thenReturn(response);

        String payload = """
                {
                  "titulo": "Alagamento Doca",
                  "dataOcorrencia": "2026-10-08",
                  "horaInicio": "14:00"
                }
                """;

        mockMvc.perform(post("/ocorrencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.protocolo").value("OC-10-1"))
                .andExpect(jsonPath("$.titulo").value("Alagamento Doca"));
    }

    @Test
    @DisplayName("GET /ocorrencias deve retornar 200 OK com lista de ocorrências")
    void deveListarOcorrencias() throws Exception {
        OcorrenciaResponse oc = OcorrenciaResponse.builder()
                .id(1L)
                .protocolo("OC-10-1")
                .titulo("Alagamento Doca")
                .build();

        when(ocorrenciaService.listarPorUsuario(any(), any())).thenReturn(List.of(oc));

        mockMvc.perform(get("/ocorrencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].protocolo").value("OC-10-1"));
    }

    @Test
    @DisplayName("GET /ocorrencias/{id} deve retornar 200 OK com dados da ocorrência")
    void deveBuscarOcorrenciaPorId() throws Exception {
        OcorrenciaResponse oc = OcorrenciaResponse.builder()
                .id(1L)
                .protocolo("OC-10-1")
                .titulo("Alagamento Doca")
                .build();

        when(ocorrenciaService.buscarPorId(any(), eq(1L))).thenReturn(oc);

        mockMvc.perform(get("/ocorrencias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.protocolo").value("OC-10-1"));
    }

    @Test
    @DisplayName("GET /ocorrencias/protocolo/{protocolo} deve retornar 200 OK com dados da ocorrência")
    void deveBuscarOcorrenciaPorProtocolo() throws Exception {
        OcorrenciaResponse oc = OcorrenciaResponse.builder()
                .id(1L)
                .protocolo("OC-10-1")
                .titulo("Alagamento Doca")
                .build();

        when(ocorrenciaService.buscarPorProtocolo("OC-10-1")).thenReturn(oc);

        mockMvc.perform(get("/ocorrencias/protocolo/OC-10-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolo").value("OC-10-1"));
    }

    @Test
    @DisplayName("POST /ocorrencias/{id}/fotos deve retornar 201 Created ao anexar foto")
    void deveRegistrarFotoComSucesso() throws Exception {
        OcorrenciaFotoResponse fotoResponse = OcorrenciaFotoResponse.builder()
                .id(10L)
                .nomeArquivo("foto.jpg")
                .urlFoto("uploads/ocorrencias/foto.jpg")
                .build();

        when(ocorrenciaService.adicionarFoto(any(), eq(1L), any())).thenReturn(fotoResponse);

        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "foto.jpg", MediaType.IMAGE_JPEG_VALUE, "imagem-bytes".getBytes()
        );

        mockMvc.perform(multipart("/ocorrencias/1/fotos").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nomeArquivo").value("foto.jpg"))
                .andExpect(jsonPath("$.urlFoto").value("uploads/ocorrencias/foto.jpg"));
    }
}
