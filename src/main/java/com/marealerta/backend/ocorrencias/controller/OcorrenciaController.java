package com.marealerta.backend.ocorrencias.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ocorrencias")
public class OcorrenciaController {

    @GetMapping
    public void listarHistorico(){

    }

    @PostMapping
    public void registrarOcorrencia(){

    }

    @GetMapping("/{id}")
    public void verDetalhe(){

    }
}
