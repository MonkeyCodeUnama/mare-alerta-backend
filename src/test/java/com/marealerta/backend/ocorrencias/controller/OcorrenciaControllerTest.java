package com.marealerta.backend.ocorrencias.controller;

import com.marealerta.backend.ocorrencias.dto.OcorrenciaFotoResponse;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaRequest;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaResponse;
import com.marealerta.backend.ocorrencias.service.OcorrenciaService;
import com.marealerta.backend.usuario.model.Usuario;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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

