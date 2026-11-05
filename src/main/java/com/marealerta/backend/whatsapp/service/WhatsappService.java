package com.marealerta.backend.whatsapp.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.PageRequest;
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
    private ConfiguracaoWhatsapp config() {
        return repository.findFirstByAtivoTrueOrderByIdAsc().orElseGet(() -> { var c = new ConfiguracaoWhatsapp(); c.setInstancia("marealerta_belem"); return repository.save(c); });
    }
    public WhatsappStatusResponse status() {
        var c = config(); Map<String,Object> state = evolution.consultarEstadoConexao();
        Object instance = evolution.consultarInstancia();
        Map<?, ?> details = instance instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map ? map : instance instanceof Map<?, ?> map ? map : Map.of();
        String telefone = c.getTelefoneRemetente() != null ? c.getTelefoneRemetente() : text(details.get("number"));
        if (telefone == null) telefone = text(details.get("ownerJid"));
        if (telefone != null) telefone = telefone.replace("@s.whatsapp.net", "");
        String nome = text(details.get("profileName"));
        String foto = text(details.get("profilePicUrl"));
        String estado = text(state.get("state"));
        if (estado == null) estado = text(details.get("connectionStatus"));
        return new WhatsappStatusResponse(c.getInstancia(), estado == null ? "UNKNOWN" : estado, telefone, nome, foto, c.isAtivo(), c.isEnviarAlertasAutomaticos());
    }

    private static String text(Object value) { return value == null ? null : String.valueOf(value); }
    public Map<String,Object> qr() { return evolution.obterQrCode(); }
    public List<WhatsappHistoricoResponse> historico(int limite) { return historico.findAllByOrderByCriadoEmDesc(PageRequest.of(0, Math.min(Math.max(limite, 1), 100))).stream().map(e -> new WhatsappHistoricoResponse(e.getId(), mascarar(e.getTelefoneDestinatario()), e.getStatusEnvio().name(), e.getErroMensagem(), e.getCriadoEm(), e.getEnviadoEm())).toList(); }
    private String mascarar(String telefone) { if (telefone == null || telefone.length() < 4) return "***"; return "***" + telefone.substring(telefone.length() - 4); }
    public Map<String,Object> conectar() { return evolution.conectar(); }
    public Map<String,Object> desconectar() { return evolution.desconectar(); }
    public Map<String,Object> reconectar() { return evolution.reiniciarInstancia(); }
    public void teste(String telefone, String mensagem) { evolution.enviarTexto(telefone, mensagem); }
    @Transactional public WhatsappStatusResponse atualizar(boolean ativo, boolean automatico) { var c=config(); c.setAtivo(ativo); c.setEnviarAlertasAutomaticos(automatico); repository.save(c); return status(); }
}
