package com.marealerta.backend.estabelecimento.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marealerta.backend.estabelecimento.dto.EstabelecimentoAtualizacaoRequest;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.usuario.model.Usuario;
import com.marealerta.backend.usuario.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstabelecimentoService {

    private final EstabelecimentoRepository estabelecimentoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public EstabelecimentoResponse buscarPorId(Long id) {
        Estabelecimento estab = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com ID: " + id));
        return EstabelecimentoResponse.fromEntity(estab);
    }

    @Transactional(readOnly = true)
    public EstabelecimentoResponse buscarPorEmailUsuario(String email) {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com e-mail: " + email));

        if (usuario.getEstabelecimento() == null) {
            throw new IllegalArgumentException("Usuário não possui estabelecimento vinculado");
        }

        return EstabelecimentoResponse.fromEntity(usuario.getEstabelecimento());
    }

    @Transactional(readOnly = true)
    public List<EstabelecimentoResponse> listarTodos() {
        return estabelecimentoRepository.findByAtivoTrue().stream()
                .map(EstabelecimentoResponse::fromEntity)
                .toList();
    }

    @Transactional
    public EstabelecimentoResponse atualizar(Long id, EstabelecimentoAtualizacaoRequest request) {
        Estabelecimento estab = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com ID: " + id));

        if (request.getNome() != null && !request.getNome().isBlank()) {
            estab.setNome(request.getNome());
        }
        if (request.getDocumento() != null && !request.getDocumento().isBlank()) {
            estab.setDocumento(request.getDocumento());
        }
        if (request.getResponsavel() != null && !request.getResponsavel().isBlank()) {
            estab.setResponsavel(request.getResponsavel());
        }
        if (request.getBairro() != null) {
            estab.setBairro(request.getBairro());
        }
        if (request.getLogradouro() != null && !request.getLogradouro().isBlank()) {
            estab.setLogradouro(request.getLogradouro());
        }
        if (request.getNumero() != null && !request.getNumero().isBlank()) {
            estab.setNumero(request.getNumero());
        }
        if (request.getComplemento() != null) {
            estab.setComplemento(request.getComplemento());
        }
        if (request.getCep() != null) {
            estab.setCep(request.getCep());
        }
        if (request.getMunicipio() != null && !request.getMunicipio().isBlank()) {
            estab.setMunicipio(request.getMunicipio());
        }
        if (request.getUf() != null && !request.getUf().isBlank()) {
            estab.setUf(request.getUf());
        }
        if (request.getTelefone() != null) {
            estab.setTelefone(request.getTelefone());
        }
        if (request.getTipoNegocio() != null) {
            estab.setTipoNegocio(request.getTipoNegocio());
        }

        estabelecimentoRepository.save(estab);
        return EstabelecimentoResponse.fromEntity(estab);
    }
}
