package com.marealerta.backend.auth.controller;

import com.marealerta.backend.auth.dto.LoginRequest;
import com.marealerta.backend.auth.dto.RefreshTokenRequest;
import com.marealerta.backend.auth.dto.RegisterRequest;
import com.marealerta.backend.auth.dto.TokenResponse;
import com.marealerta.backend.auth.service.AuthService;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.service.EstabelecimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints de login, registro e renovação de tokens JWT")
public class AuthController {

    private final AuthService authService;
    private final EstabelecimentoService estabelecimentoService;

    @Operation(summary = "Realizar login e obter par de tokens (Access + Refresh)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest login) {
        return ResponseEntity.ok(authService.login(login));
    }

    @Operation(summary = "Cadastrar novo estabelecimento e usuário gestor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ou CNPJ já em uso")
    })
    @PostMapping("/registro")
    public ResponseEntity<TokenResponse> register(@RequestBody @Valid RegisterRequest register) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(register));
    }

    @Operation(summary = "Renovar Access Token a partir de um Refresh Token válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Novo Access Token gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh Token inválido ou expirado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @Operation(summary = "Obter dados do usuário autenticado")
    @GetMapping("/me")
    public ResponseEntity<EstabelecimentoResponse> me(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(estabelecimentoService.buscarPorEmailUsuario(principal.getName()));
    }
}
