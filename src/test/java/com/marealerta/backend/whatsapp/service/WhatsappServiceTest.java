package com.marealerta.backend.whatsapp.service;

import com.marealerta.backend.alerta.client.EvolutionApiClient;
import com.marealerta.backend.alerta.model.EnvioAlertaWhatsapp;
import com.marealerta.backend.alerta.model.StatusEnvioWhatsapp;
import com.marealerta.backend.alerta.repository.EnvioAlertaWhatsappRepository;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.whatsapp.dto.WhatsappHistoricoResponse;
import com.marealerta.backend.whatsapp.dto.WhatsappStatusResponse;
import com.marealerta.backend.whatsapp.model.ConfiguracaoWhatsapp;
import com.marealerta.backend.whatsapp.repository.ConfiguracaoWhatsappRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsappServiceTest {

    @Mock
    private EvolutionApiClient evolution;

    @Mock
    private ConfiguracaoWhatsappRepository repository;

    @Mock
    private EnvioAlertaWhatsappRepository historicoRepository;

    @InjectMocks
    private WhatsappService whatsappService;

    @Test
    @DisplayName("historico deve expor nome do estabelecimento e responsavel junto ao telefone mascarado")
    void deveExporEstabelecimentoEResponsavelNoHistorico() {
        Estabelecimento estab = Estabelecimento.builder()
                .id(101L)
                .nome("Mercearia Ver-o-Peso")
                .responsavel("Ana Claudia")
                .build();

        EnvioAlertaWhatsapp envio = EnvioAlertaWhatsapp.builder()
                .id(1L)
                .estabelecimento(estab)
                .telefoneDestinatario("5591984120099")
                .statusEnvio(StatusEnvioWhatsapp.ENVIADO)
                .tentativas(1)
                .criadoEm(Instant.now())
                .enviadoEm(Instant.now())
                .build();

        when(historicoRepository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, 30)))
                .thenReturn(List.of(envio));

        List<WhatsappHistoricoResponse> result = whatsappService.historico(30);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).estabelecimentoNome()).isEqualTo("Mercearia Ver-o-Peso");
        assertThat(result.get(0).responsavel()).isEqualTo("Ana Claudia");
        assertThat(result.get(0).telefone()).isEqualTo("***0099");
    }

    @Test
    @DisplayName("status nao deve expor ownerJid residual quando instancia foi removida ou desconectada")
    void deveOcultarTelefoneQuandoInstanciaDesconectada() {
        ConfiguracaoWhatsapp cfg = new ConfiguracaoWhatsapp();
        cfg.setInstancia("marealerta_belem");
        cfg.setAtivo(true);
        cfg.setEnviarAlertasAutomaticos(true);

        when(repository.findByInstancia(any())).thenReturn(Optional.empty());
        when(repository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(cfg));
        when(evolution.consultarEstadoConexao()).thenReturn(Map.of(
                "instance", Map.of("instanceName", "marealerta_belem", "state", "connecting")
        ));
        when(evolution.consultarInstancia()).thenReturn(List.of(Map.of(
                "name", "marealerta_belem",
                "connectionStatus", "connecting",
                "ownerJid", "559193649983@s.whatsapp.net",
                "profileName", "Comercio Teste",
                "profilePicUrl", "https://example.com/pic.jpg",
                "disconnectionReasonCode", 401
        )));

        WhatsappStatusResponse status = whatsappService.status();

        assertThat(status.estado()).isEqualTo("close");
        assertThat(status.telefone()).isNull();
        assertThat(status.nome()).isNull();
        assertThat(status.foto()).isNull();
    }
}
