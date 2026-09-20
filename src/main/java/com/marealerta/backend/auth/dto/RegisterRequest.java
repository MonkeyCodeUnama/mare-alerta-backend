package com.marealerta.backend.auth.dto;

import com.marealerta.backend.estabelecimento.dto.EstabelecimentoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String senha;

    @NotNull(message = "nao deve ser nulo")
    @Valid
    private EstabelecimentoRequest estabelecimento;

}
