package com.marealerta.backend.auth.controller;

import com.marealerta.backend.auth.dto.LoginRequest;
import com.marealerta.backend.auth.dto.RegisterRequest;
import com.marealerta.backend.auth.dto.TokenResponse;
import com.marealerta.backend.usuario.model.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.marealerta.backend.auth.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private TokenResponse TokenR;

    @PostMapping("/registro")
    public ResponseEntity<TokenResponse> cadastrar(@RequestBody @Valid RegisterRequest registerRequest) throws BadRequestException {
        TokenR = authService.register(registerRequest);
        return ResponseEntity.ok(TokenR);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) throws BadRequestException{
        TokenR = authService.login(loginRequest);
        return ResponseEntity.ok(TokenR);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Usuario usuario){
        return ResponseEntity.ok(usuario.getUsername());
    }

}
