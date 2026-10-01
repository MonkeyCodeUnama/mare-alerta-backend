package com.marealerta.backend.ocorrencias.dto;

import java.time.LocalDateTime;

import com.marealerta.backend.ocorrencias.model.OcorrenciaFoto;

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
public class OcorrenciaFotoResponse {

    private Long id;
    private String nomeArquivo;
    private String urlFoto;
    private Long tamanhoBytes;
    private String contentType;
    private LocalDateTime createdAt;

    public static OcorrenciaFotoResponse fromEntity(OcorrenciaFoto f) {
        if (f == null) return null;
        return OcorrenciaFotoResponse.builder()
                .id(f.getId())
                .nomeArquivo(f.getNomeArquivo())
                .urlFoto(f.getUrlFoto())
                .tamanhoBytes(f.getTamanhoBytes())
                .contentType(f.getContentType())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
