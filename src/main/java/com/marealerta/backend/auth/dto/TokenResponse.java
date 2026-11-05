package com.marealerta.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponse {

    private String token;
    private String refreshToken;
    @Builder.Default
    private String tipo = "Bearer";
    private Long expiraEm;
    private Long estabelecimentoId;
    private String email;
    private String role;

}
