package com.marealerta.backend.whatsapp.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import com.marealerta.backend.alerta.repository.EnvioAlertaWhatsappRepository;
import com.marealerta.backend.whatsapp.dto.WhatsappHistoricoResponse;
import com.marealerta.backend.alerta.client.EvolutionApiClient;
import com.marealerta.backend.whatsapp.dto.WhatsappStatusResponse;
import com.marealerta.backend.whatsapp.model.ConfiguracaoWhatsapp;
import com.marealerta.backend.whatsapp.repository.ConfiguracaoWhatsappRepository;

@Service @RequiredArgsConstructor
public class WhatsappService {
    private final EvolutionApiClient evolution;
    private final ConfiguracaoWhatsappRepository repository;
    private final EnvioAlertaWhatsappRepository historico;

    @Value("${whatsapp.evolution.instancia:marealerta_belem}")
    private String instanciaPadrao;

    private ConfiguracaoWhatsapp config() {
        return repository.findByInstancia(instanciaPadrao)
                .or(() -> repository.findFirstByOrderByIdAsc())
                .orElseGet(() -> {
                    var c = new ConfiguracaoWhatsapp();
                    c.setInstancia(instanciaPadrao);
                    return repository.save(c);
                });
    }
    public WhatsappStatusResponse status() {
        var c = config();
        Map<String, Object> state = Map.of();
        Object instance = List.of();
        try {
            state = evolution.consultarEstadoConexao();
            instance = evolution.consultarInstancia();
        } catch (RestClientException ignored) {
            return new WhatsappStatusResponse(c.getInstancia(), "OFFLINE", null, null, null, c.isAtivo(), c.isEnviarAlertasAutomaticos());
        }

        Map<?, ?> details = instance instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map
                ? map
                : instance instanceof Map<?, ?> map ? map : Map.of();

        String estado = text(state.get("state"));
        if (estado == null && state.get("instance") instanceof Map<?, ?> nested) {
            estado = text(nested.get("state"));
        }
        if (estado == null) {
            estado = text(details.get("connectionStatus"));
        }

        boolean isConnected = "open".equalsIgnoreCase(estado);
        String disconnectionCode = text(details.get("disconnectionReasonCode"));
        if (!isConnected && "401".equals(disconnectionCode)) {
            estado = "close";
        }

        String telefone = null;
        String nome = null;
        String foto = null;
        if (isConnected) {
            telefone = c.getTelefoneRemetente() != null ? c.getTelefoneRemetente() : text(details.get("number"));
            if (telefone == null) telefone = text(details.get("ownerJid"));
            if (telefone != null) telefone = telefone.replace("@s.whatsapp.net", "");
            nome = text(details.get("profileName"));
            foto = text(details.get("profilePicUrl"));
        }

        return new WhatsappStatusResponse(c.getInstancia(), estado == null ? "UNKNOWN" : estado, telefone, nome, foto, c.isAtivo(), c.isEnviarAlertasAutomaticos());
    }

    private static String text(Object value) { return value == null ? null : String.valueOf(value); }
    public Map<String,Object> qr() { return evolution.obterQrCode(); }
    @Transactional(readOnly = true)
    public List<WhatsappHistoricoResponse> historico(int limite) {
        return historico.findAllByOrderByCriadoEmDesc(PageRequest.of(0, Math.min(Math.max(limite, 1), 100))).stream()
                .map(e -> new WhatsappHistoricoResponse(
                        e.getId(),
                        mascarar(e.getTelefoneDestinatario()),
                        e.getStatusEnvio().name(),
                        e.getErroMensagem(),
                        e.getCriadoEm(),
                        e.getEnviadoEm(),
                        e.getEstabelecimento() != null ? e.getEstabelecimento().getNome() : null,
                        e.getEstabelecimento() != null ? e.getEstabelecimento().getResponsavel() : null
                ))
                .toList();
    }
    private String mascarar(String telefone) { if (telefone == null || telefone.length() < 4) return "***"; return "***" + telefone.substring(telefone.length() - 4); }
    public Map<String,Object> conectar() { return evolution.conectar(); }
    @Transactional
    public Map<String,Object> desconectar() {
        var res = evolution.desconectar();
        var c = config();
        if (c.getTelefoneRemetente() != null) {
            c.setTelefoneRemetente(null);
            repository.save(c);
        }
        return res;
    }
    public Map<String,Object> reconectar() { return evolution.reiniciarInstancia(); }
    public void teste(String telefone, String mensagem) {
        WhatsappStatusResponse atual = status();
        if (!"open".equalsIgnoreCase(atual.estado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O WhatsApp ainda não está conectado. Gere e leia o QR Code primeiro.");
        }
        try {
            evolution.enviarTexto(telefone, mensagem);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível enviar a mensagem pela Evolution API. Verifique se o número possui WhatsApp e se a sessão está ativa.");
        }
    }
    @Transactional public WhatsappStatusResponse atualizar(boolean ativo, boolean automatico) { var c=config(); c.setAtivo(ativo); c.setEnviarAlertasAutomaticos(automatico); repository.save(c); return status(); }
}
