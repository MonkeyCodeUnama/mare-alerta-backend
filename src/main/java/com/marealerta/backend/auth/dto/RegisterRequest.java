package com.marealerta.backend.auth.dto;

import com.marealerta.backend.estabelecimento.dto.EstabelecimentoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 8, message = "a senha deve ter no mínimo 8 caracteres")
    private String senha;

    @NotNull(message = "nao deve ser nulo")
    @Valid
    private EstabelecimentoRequest estabelecimento;

}
