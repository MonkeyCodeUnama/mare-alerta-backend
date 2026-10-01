package com.marealerta.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class TokenResponse {

    @NotBlank
    private String token;

    private Long estabelecimentoId;

    private String email;

    public TokenResponse(String token) {
        this.token = token;
    }
}
