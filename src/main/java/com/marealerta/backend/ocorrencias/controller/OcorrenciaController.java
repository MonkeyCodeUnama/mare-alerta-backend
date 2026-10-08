package com.marealerta.backend.ocorrencias.controller;

import com.marealerta.backend.ocorrencias.dto.OcorrenciaFotoResponse;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaRequest;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaResponse;
import com.marealerta.backend.ocorrencias.service.OcorrenciaService;
import com.marealerta.backend.usuario.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ocorrencias")
@RequiredArgsConstructor
@Tag(name = "Ocorrencia", description = "Endpoints de ocorrencias e fotos")
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;

    @Operation(summary = "resgitrar ocorrencia do usuario")
    @PostMapping
    public ResponseEntity<OcorrenciaResponse> registrarOcorrencia(@AuthenticationPrincipal  Usuario usuario, @RequestBody OcorrenciaRequest ocorrenciaRequest){
        return ResponseEntity.ok(ocorrenciaService.registrarO(usuario, ocorrenciaRequest));
    }

    @Operation(summary = "resgitrar foto da ocorrencia do usuario")
    @PostMapping(value = "/{ocorrenciaId}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcorrenciaFotoResponse> registrarFoto(@AuthenticationPrincipal Usuario usuario, @PathVariable Long ocorrenciaId, @RequestParam("arquivo")MultipartFile arquivo) throws IOException {
        return ResponseEntity.ok(ocorrenciaService.adicionarFoto(usuario, ocorrenciaId, arquivo));
    }

}
