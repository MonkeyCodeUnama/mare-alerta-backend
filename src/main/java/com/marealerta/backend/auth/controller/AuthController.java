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
@Tag(name = "Autenticação", description = "Endpoints de autenticação, registro e renovação de token JWT")
public class AuthController {

    private final AuthService authService;
    private final EstabelecimentoService estabelecimentoService;

    @Operation(summary = "Cadastrar um novo usuário e estabelecimento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "usuário criado"),
            @ApiResponse(responseCode = "400", description = "dados inválidos"),
            @ApiResponse(responseCode = "409", description = "e-mail já cadastrado")
    })
    @PostMapping("/registro")
    public ResponseEntity<TokenResponse> cadastrar(@RequestBody @Valid RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @Operation(summary = "Login do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "autenticação realizada"),
            @ApiResponse(responseCode = "400", description = "dados inválidos"),
            @ApiResponse(responseCode = "401", description = "credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Operation(summary = "Renovar token de acesso (Refresh Token)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "token atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "dados inválidos"),
            @ApiResponse(responseCode = "401", description = "refresh token inválido ou expirado")
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
