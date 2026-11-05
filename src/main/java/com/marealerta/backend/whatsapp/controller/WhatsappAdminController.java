package com.marealerta.backend.whatsapp.controller;

import java.util.Map;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.marealerta.backend.whatsapp.dto.*;
import com.marealerta.backend.whatsapp.service.WhatsappService;

@RestController @RequestMapping("/admin/whatsapp") @RequiredArgsConstructor
public class WhatsappAdminController {
    private final WhatsappService service;
    @GetMapping("/status") public WhatsappStatusResponse status() { return service.status(); }
    @GetMapping("/qr") public Map<String,Object> qr() { return service.qr(); }
    @GetMapping("/configuracao") public WhatsappStatusResponse configuracao() { return service.status(); }
    @GetMapping("/history") public List<WhatsappHistoricoResponse> history(@RequestParam(defaultValue = "50") int limit) { return service.historico(limit); }
    @GetMapping("/envios") public List<WhatsappHistoricoResponse> envios(@RequestParam(defaultValue = "50") int limit) { return service.historico(limit); }
    @PostMapping("/connect") public Map<String,Object> connect() { return service.conectar(); }
    @PostMapping("/reconnect") public Map<String,Object> reconnect() { return service.reconectar(); }
    @PostMapping("/disconnect") public Map<String,Object> disconnect() { return service.desconectar(); }
    @PostMapping("/test") public ResponseEntity<Void> test(@Valid @RequestBody WhatsappTestRequest request) { service.teste(request.telefone(), request.mensagem()); return ResponseEntity.noContent().build(); }
    @PatchMapping("/settings") public WhatsappStatusResponse settings(@RequestParam boolean ativo, @RequestParam boolean enviarAlertasAutomaticos) { return service.atualizar(ativo, enviarAlertasAutomaticos); }
}
