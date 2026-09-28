package com.marealerta.backend.alerta.dto;

import java.time.LocalDateTime;

import com.marealerta.backend.alerta.model.Alerta;
import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.alerta.model.OrigemAlerta;
import com.marealerta.backend.alerta.model.TipoAlerta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaResponse {

    private Long id;
    private Long estabelecimentoId;
    private String estabelecimentoNome;
    private NivelRisco nivelRisco;
    private String mensagem;
    private TipoAlerta tipoAlerta;
    private OrigemAlerta origemAlerta;
    private LocalDateTime dataEnvio;

    public static AlertaResponse fromEntity(Alerta a) {
        if (a == null) return null;
        return AlertaResponse.builder()
                .id(a.getId())
                .estabelecimentoId(a.getEstabelecimento() != null ? a.getEstabelecimento().getId() : null)
                .estabelecimentoNome(a.getEstabelecimento() != null ? a.getEstabelecimento().getNome() : null)
                .nivelRisco(a.getNivelRisco())
                .mensagem(a.getMensagem())
                .tipoAlerta(a.getTipoAlerta())
                .origemAlerta(a.getOrigemAlerta())
                .dataEnvio(a.getDataEnvio())
                .build();
    }
}
