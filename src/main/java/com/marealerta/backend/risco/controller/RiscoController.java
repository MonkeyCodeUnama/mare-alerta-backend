package com.marealerta.backend.risco.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class RiscoController {

    @GetMapping("/risco/atual")
    public void consultarRisco(){

    }
}
