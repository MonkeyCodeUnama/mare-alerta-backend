package com.marealerta.backend.alerta.dto;

import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.alerta.model.OrigemAlerta;
import com.marealerta.backend.alerta.model.TipoAlerta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AlertaRequest {

    // Mantido como campo compatível com clientes antigos; a API usa o estabelecimento do JWT.
    private Long estabelecimentoId;

    @NotNull(message = "O nível de risco é obrigatório")
    private NivelRisco nivelRisco;

    @NotBlank(message = "A mensagem é obrigatória")
    private String mensagem;

    @NotNull(message = "O tipo de alerta é obrigatório")
    private TipoAlerta tipoAlerta;

    @Builder.Default
    private OrigemAlerta origemAlerta = OrigemAlerta.AUTOMATICO;
}
