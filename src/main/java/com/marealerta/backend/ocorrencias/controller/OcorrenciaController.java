package com.marealerta.backend.ocorrencias.controller;

import com.marealerta.backend.ocorrencias.dto.OcorrenciaFotoResponse;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaRequest;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaResponse;
import com.marealerta.backend.ocorrencias.model.StatusOcorrencia;
import com.marealerta.backend.ocorrencias.service.OcorrenciaService;
import com.marealerta.backend.usuario.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ocorrencias")
@RequiredArgsConstructor
@Tag(name = "Ocorrencias", description = "Endpoints de ocorrências e fotos")
@SecurityRequirement(name = "bearerAuth")
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;

    @Operation(summary = "Registrar ocorrência do usuário")
    @ApiResponse(responseCode = "201", description = "Ocorrência registrada")
    @PostMapping
    public ResponseEntity<OcorrenciaResponse> registrarOcorrencia(
            @AuthenticationPrincipal Usuario usuario,
            @RequestBody OcorrenciaRequest ocorrenciaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ocorrenciaService.registrarO(usuario, ocorrenciaRequest));
    }

    @Operation(summary = "Listar ocorrências do usuário autenticado")
    @GetMapping
    public ResponseEntity<List<OcorrenciaResponse>> listarOcorrencias(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) StatusOcorrencia status) {
        return ResponseEntity.ok(ocorrenciaService.listarPorUsuario(usuario, status));
    }

    @Operation(summary = "Buscar ocorrência por ID")
    @GetMapping("/{id}")
    public ResponseEntity<OcorrenciaResponse> buscarPorId(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long id) {
        return ResponseEntity.ok(ocorrenciaService.buscarPorId(usuario, id));
    }

    @Operation(summary = "Buscar ocorrência por protocolo")
    @GetMapping("/protocolo/{protocolo}")
    public ResponseEntity<OcorrenciaResponse> buscarPorProtocolo(@PathVariable String protocolo) {
        return ResponseEntity.ok(ocorrenciaService.buscarPorProtocolo(protocolo));
    }

    @Operation(summary = "Registrar foto da ocorrência do usuário")
    @PostMapping(value = "/{ocorrenciaId}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcorrenciaFotoResponse> registrarFoto(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long ocorrenciaId,
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(ocorrenciaService.adicionarFoto(usuario, ocorrenciaId, arquivo));
    }

    @Operation(summary = "Baixar foto da ocorrência do usuário")
    @GetMapping("/{ocorrenciaId}/fotos/{fotoId}")
    public ResponseEntity<Resource> baixarFoto(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long ocorrenciaId,
            @PathVariable Long fotoId) throws IOException {
        return ocorrenciaService.baixarFoto(usuario, ocorrenciaId, fotoId);
    }
}
