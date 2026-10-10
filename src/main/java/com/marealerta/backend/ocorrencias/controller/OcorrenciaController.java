package com.marealerta.backend.ocorrencias.controller;

import com.marealerta.backend.ocorrencias.dto.OcorrenciaFotoResponse;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaRequest;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaResponse;
import com.marealerta.backend.ocorrencias.service.OcorrenciaService;
import com.marealerta.backend.usuario.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ocorrencias")
@RequiredArgsConstructor
<<<<<<< HEAD
@Tag(name = "Ocorrencia", description = "Endpoints de ocorrencias e fotos")
@SecurityRequirement(name = "bearerAuth")
=======
@Tag(name = "Ocorrencias", description = "Endpoints de ocorrências e fotos")
>>>>>>> 5881426 (fix: corrige bugs em ocorrencias, risco e telemetria e adiciona testes unitarios)
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;

    @Operation(summary = "Registrar ocorrência do usuário")
    @PostMapping
<<<<<<< HEAD
    @ApiResponse(responseCode = "201", description = "Ocorrência registrada")
    public ResponseEntity<OcorrenciaResponse> registrarOcorrencia(@AuthenticationPrincipal  Usuario usuario, @RequestBody OcorrenciaRequest ocorrenciaRequest){
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ocorrenciaService.registrarO(usuario, ocorrenciaRequest));
=======
    public ResponseEntity<OcorrenciaResponse> registrarOcorrencia(@AuthenticationPrincipal Usuario usuario, @RequestBody OcorrenciaRequest ocorrenciaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ocorrenciaService.registrarO(usuario, ocorrenciaRequest));
>>>>>>> 5881426 (fix: corrige bugs em ocorrencias, risco e telemetria e adiciona testes unitarios)
    }

    @Operation(summary = "Registrar foto da ocorrência do usuário")
    @PostMapping(value = "/{ocorrenciaId}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcorrenciaFotoResponse> registrarFoto(@AuthenticationPrincipal Usuario usuario, @PathVariable Long ocorrenciaId, @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(ocorrenciaService.adicionarFoto(usuario, ocorrenciaId, arquivo));
    }

}
