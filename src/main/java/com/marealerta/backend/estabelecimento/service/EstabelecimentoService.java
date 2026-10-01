package com.marealerta.backend.estabelecimento.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marealerta.backend.estabelecimento.dto.EstabelecimentoAtualizacaoRequest;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.model.EstabelecimentoHorario;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoHorarioRepository;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.itens.model.ItemCritico;
import com.marealerta.backend.itens.repository.ItensCriticosRepository;
import com.marealerta.backend.usuario.model.Usuario;
import com.marealerta.backend.usuario.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstabelecimentoService {

    private final EstabelecimentoRepository estabelecimentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstabelecimentoHorarioRepository estabelecimentoHorarioRepository;
    private final ItensCriticosRepository itensCriticosRepository;

    @Transactional(readOnly = true)
    public EstabelecimentoResponse buscarPorId(Long id) {
        Estabelecimento estab = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com ID: " + id));
        return buildResponse(estab);
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

        return buildResponse(usuario.getEstabelecimento());
    }

    @Transactional(readOnly = true)
    public List<EstabelecimentoResponse> listarTodos() {
        return estabelecimentoRepository.findByAtivoTrue().stream()
                .map(this::buildResponse)
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

        if (request.getDias() != null && !request.getDias().isEmpty()) {
            estabelecimentoHorarioRepository.deleteByEstabelecimentoId(estab.getId());

            LocalTime abertura = parseHorario(request.getHorarioAbertura(), LocalTime.of(7, 30));
            LocalTime fechamento = parseHorario(request.getHorarioFechamento(), LocalTime.of(19, 0));

            for (String diaStr : request.getDias()) {
                Integer diaSemana = parseDiaSemana(diaStr);
                if (diaSemana != null) {
                    EstabelecimentoHorario horario = new EstabelecimentoHorario();
                    horario.setEstabelecimento(estab);
                    horario.setDiaSemana(diaSemana);
                    horario.setHorarioAbertura(abertura);
                    horario.setHorarioFechamento(fechamento);
                    estabelecimentoHorarioRepository.save(horario);
                }
            }
        }

        if (request.getItensCriticos() != null) {
            itensCriticosRepository.deleteByEstabelecimentoId(estab.getId());

            for (String itemNome : request.getItensCriticos()) {
                if (itemNome != null && !itemNome.isBlank()) {
                    ItemCritico ic = new ItemCritico();
                    ic.setNome(itemNome.trim());
                    ic.setEstabelecimento(estab);
                    itensCriticosRepository.save(ic);
                }
            }
        }

        return buildResponse(estab);
    }

    private EstabelecimentoResponse buildResponse(Estabelecimento estab) {
        List<EstabelecimentoHorario> horarios = estabelecimentoHorarioRepository.findByEstabelecimentoId(estab.getId());
        List<String> dias = horarios.stream()
                .map(h -> diaSemanaToString(h.getDiaSemana()))
                .distinct()
                .toList();

        String abertura = horarios.isEmpty() ? null : horarios.get(0).getHorarioAbertura().toString();
        String fechamento = horarios.isEmpty() ? null : horarios.get(0).getHorarioFechamento().toString();

        List<ItemCritico> itens = itensCriticosRepository.findByEstabelecimentoId(estab.getId());
        List<String> nomesItens = itens.stream()
                .map(ItemCritico::getNome)
                .toList();

        return EstabelecimentoResponse.fromEntity(estab, dias, abertura, fechamento, nomesItens);
    }

    private Integer parseDiaSemana(String dia) {
        if (dia == null || dia.isBlank()) return null;
        String clean = dia.trim().toLowerCase();
        if (clean.startsWith("dom") || clean.equals("1")) return 1;
        if (clean.startsWith("seg") || clean.equals("2")) return 2;
        if (clean.startsWith("ter") || clean.equals("3")) return 3;
        if (clean.startsWith("qua") || clean.equals("4")) return 4;
        if (clean.startsWith("qui") || clean.equals("5")) return 5;
        if (clean.startsWith("sex") || clean.equals("6")) return 6;
        if (clean.startsWith("sáb") || clean.startsWith("sab") || clean.equals("7")) return 7;
        return null;
    }

    private String diaSemanaToString(Integer dia) {
        if (dia == null) return null;
        return switch (dia) {
            case 1 -> "Dom";
            case 2 -> "Seg";
            case 3 -> "Ter";
            case 4 -> "Qua";
            case 5 -> "Qui";
            case 6 -> "Sex";
            case 7 -> "Sáb";
            default -> String.valueOf(dia);
        };
    }

    private LocalTime parseHorario(String horarioStr, LocalTime defaultTime) {
        if (horarioStr == null || horarioStr.isBlank()) return defaultTime;
        String clean = horarioStr.trim().toUpperCase();
        try {
            if (clean.endsWith("AM") || clean.endsWith("PM")) {
                DateTimeFormatter ampm = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
                return LocalTime.parse(clean, ampm);
            }
            if (clean.length() == 5) {
                return LocalTime.parse(clean, DateTimeFormatter.ofPattern("HH:mm"));
            }
            return LocalTime.parse(clean);
        } catch (Exception e) {
            return defaultTime;
        }
    }
}
