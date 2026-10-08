package com.marealerta.backend.risco.controller;

import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.risco.service.RiscoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/risco")
@RequiredArgsConstructor
public class RiscoController {

    private final RiscoService riscoService;

    @GetMapping("/atual")
    public ResponseEntity<RiscoResponse> consultarRiscoAtual(@RequestParam(defaultValue = "BELEM_CENTRO") String localidade){
        RiscoResponse response = riscoService.processarEObterRiscoParaLocalidade(localidade);
        return ResponseEntity.ok(response);

    }
}
