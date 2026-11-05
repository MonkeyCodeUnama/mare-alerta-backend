package com.marealerta.backend.risco.dto;

import com.marealerta.backend.estabelecimento.model.Bairro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BairroCatalogoDTO {

    private String codigo;
    private String descricao;
    private Double latitude;
    private Double longitude;
    private Double cotaTransbordamentoMetros;
    private String cotaFormatada;
    private String bacia;
    private String nivelVulnerabilidade;

    public static BairroCatalogoDTO fromEnum(Bairro b) {
        return BairroCatalogoDTO.builder()
                .codigo(b.name())
                .descricao(b.getDescricao())
                .latitude(b.getLatitude())
                .longitude(b.getLongitude())
                .cotaTransbordamentoMetros(b.getCotaTransbordamentoMetros())
                .cotaFormatada(b.getCotaTransbordamentoMetros() != null
                        ? String.format(Locale.US, "%.2f m", b.getCotaTransbordamentoMetros())
                        : null)
                .bacia(b.getBacia())
                .nivelVulnerabilidade(b.getNivelVulnerabilidade())
                .build();
    }
}
