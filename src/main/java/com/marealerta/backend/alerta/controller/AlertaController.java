package com.marealerta.backend.alerta.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alertas")
public class AlertaController {

    @GetMapping
    public void listar(){

    }

    @PostMapping
    public void criar(){

    }

    @GetMapping
    public void verDetalhe(){

    }
}
