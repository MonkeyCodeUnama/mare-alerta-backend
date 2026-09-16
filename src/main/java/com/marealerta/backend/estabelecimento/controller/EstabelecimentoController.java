package com.marealerta.backend.estabelecimento.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estabelecimento")
public class EstabelecimentoController {

    @PostMapping
    public void criar(){

    }

    @GetMapping("/{id}")
    public void buscarEst(){

    }

    @PutMapping("/{id}")
    public void editarEst(){

    }

    @GetMapping
    public void listar(){

    }
}
