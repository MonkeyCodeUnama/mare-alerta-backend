package com.marealerta.backend.auth.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class TokenResponse {

    @NotBlank
    private String token;
}
