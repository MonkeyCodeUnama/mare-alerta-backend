package com.marealerta.backend.auth.controller;

import com.marealerta.backend.auth.dto.LoginRequest;
import com.marealerta.backend.auth.dto.RegisterRequest;
import com.marealerta.backend.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.marealerta.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/**")
public class AuthController {

    private final AuthService authService;
    private TokenResponse TokenR;

    @Operation(summary = "cadatrar um novo suuario e estabelecimento")
    @ApiResponse(responseCode = "201", description = "usuario criado")
    @ApiResponse(responseCode = "400", description = "email ja cadastrado")
    @PostMapping("/registro")
    public ResponseEntity<TokenResponse> cadastrar(@RequestBody @Valid RegisterRequest registerRequest) throws BadRequestException {
        TokenR = authService.register(registerRequest);
        return ResponseEntity.ok(TokenR);
    }

    @Operation(summary = "login")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) throws BadRequestException{
        TokenR = authService.login(loginRequest);
        return ResponseEntity.ok(TokenR);
    }

}
