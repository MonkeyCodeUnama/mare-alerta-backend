package com.marealerta.backend.alerta.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.model.Alerta;
import com.marealerta.backend.alerta.repository.AlertaRepository;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.usuario.model.Usuario;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    @Transactional(readOnly = true)
    public List<AlertaResponse> listar() {
        return alertaRepository.findAllByOrderByDataEnvioDesc().stream()
                .map(AlertaResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AlertaResponse criar(Usuario usuario, AlertaRequest request) {
        if (usuario == null || usuario.getEstabelecimento() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuário não autenticado");
        }

        var estabelecimento = estabelecimentoRepository.findById(usuario.getEstabelecimento().getId())
                .orElseThrow(() -> new EntityNotFoundException("estabelecimento não encontrado"));

        Alerta alerta = new Alerta();
        alerta.setNivelRisco(request.getNivelRisco());
        alerta.setMensagem(request.getMensagem());
        alerta.setTipoAlerta(request.getTipoAlerta());
        alerta.setOrigemAlerta(request.getOrigemAlerta() != null
                ? request.getOrigemAlerta()
                : com.marealerta.backend.alerta.model.OrigemAlerta.AUTOMATICO);
        alerta.setEstabelecimento(estabelecimento);
        return AlertaResponse.fromEntity(alertaRepository.save(alerta));
    }

    @Transactional(readOnly = true)
    public AlertaResponse buscar(Long id) {
        return alertaRepository.findById(id)
                .map(AlertaResponse::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("alerta não encontrado"));
    }
}
