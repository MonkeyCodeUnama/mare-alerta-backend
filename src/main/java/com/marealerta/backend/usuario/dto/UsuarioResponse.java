package com.marealerta.backend.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;

    private String email;

    private String nomeEstabelecimento;

}
