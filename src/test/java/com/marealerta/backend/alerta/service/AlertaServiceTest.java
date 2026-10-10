package com.marealerta.backend.alerta.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.marealerta.backend.alerta.client.EvolutionApiClient;
import com.marealerta.backend.alerta.client.EvolutionSendResponse;
import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.model.Alerta;
import com.marealerta.backend.alerta.model.EnvioAlertaWhatsapp;
import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.alerta.model.OrigemAlerta;
import com.marealerta.backend.alerta.model.StatusEnvioWhatsapp;
import com.marealerta.backend.alerta.model.TipoAlerta;
import com.marealerta.backend.alerta.repository.AlertaRepository;
import com.marealerta.backend.alerta.repository.EnvioAlertaWhatsappRepository;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private EnvioAlertaWhatsappRepository envioAlertaWhatsappRepository;

    @Mock
    private EvolutionApiClient evolutionApiClient;

    @InjectMocks
    private AlertaService alertaService;

    private Estabelecimento estabelecimentoComTelefone;
    private Estabelecimento estabelecimentoSemTelefone;

    @BeforeEach
    void setUp() {
        estabelecimentoComTelefone = Estabelecimento.builder()
                .id(1L)
                .nome("Comércio São José")
                .telefone("91987654321")
                .bairro(Bairro.CAMPINA)
                .ativo(true)
                .build();

        estabelecimentoSemTelefone = Estabelecimento.builder()
                .id(2L)
                .nome("Padaria Belém")
                .telefone(null)
                .bairro(Bairro.CAMPINA)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve criar alerta e disparar WhatsApp quando o estabelecimento possuir telefone")
    void deveCriarAlertaEDispararWhatsappQuandoTiverTelefone() {
        AlertaRequest request = AlertaRequest.builder()
                .estabelecimentoId(1L)
                .nivelRisco(NivelRisco.ALTO)
                .mensagem("Risco iminente de alagamento")
                .tipoAlerta(TipoAlerta.ALAGAMENTO)
                .origemAlerta(OrigemAlerta.AUTOMATICO)
                .build();

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimentoComTelefone));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta a = invocation.getArgument(0);
            a.setId(10L);
            a.setDataEnvio(LocalDateTime.now());
            return a;
        });

        when(envioAlertaWhatsappRepository.save(any(EnvioAlertaWhatsapp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvolutionSendResponse mockResponse = new EvolutionSendResponse(
                new EvolutionSendResponse.Key("WPP-MSG-100", "5591987654321@s.whatsapp.net"),
                "PENDING",
                null
        );
        when(evolutionApiClient.enviarTexto(anyString(), anyString())).thenReturn(mockResponse);

        AlertaResponse response = alertaService.criarAlerta(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getEstabelecimentoNome()).isEqualTo("Comércio São José");
        assertThat(response.getNivelRisco()).isEqualTo(NivelRisco.ALTO);

        verify(evolutionApiClient).enviarTexto(anyString(), anyString());

        ArgumentCaptor<EnvioAlertaWhatsapp> captor = ArgumentCaptor.forClass(EnvioAlertaWhatsapp.class);
        verify(envioAlertaWhatsappRepository, times(2)).save(captor.capture());

        EnvioAlertaWhatsapp envioFinal = captor.getValue();
        assertThat(envioFinal.getStatusEnvio()).isEqualTo(StatusEnvioWhatsapp.ENVIADO);
        assertThat(envioFinal.getWhatsappMessageId()).isEqualTo("WPP-MSG-100");
        assertThat(envioFinal.getTentativas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve criar alerta sem disparar WhatsApp quando o estabelecimento não possuir telefone")
    void deveCriarAlertaSemDispararWhatsappQuandoNaoTiverTelefone() {
        AlertaRequest request = AlertaRequest.builder()
                .estabelecimentoId(2L)
                .nivelRisco(NivelRisco.MEDIO)
                .mensagem("Atenção com a maré subindo")
                .tipoAlerta(TipoAlerta.ALAGAMENTO)
                .build();

        when(estabelecimentoRepository.findById(2L)).thenReturn(Optional.of(estabelecimentoSemTelefone));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta a = invocation.getArgument(0);
            a.setId(20L);
            a.setDataEnvio(LocalDateTime.now());
            return a;
        });

        AlertaResponse response = alertaService.criarAlerta(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(20L);

        verify(evolutionApiClient, never()).enviarTexto(anyString(), anyString());
        verify(envioAlertaWhatsappRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve registrar status FALHA no rastreio quando a Evolution API falhar")
    void deveRegistrarStatusFalhaQuandoEvolutionApiFalhar() {
        AlertaRequest request = AlertaRequest.builder()
                .estabelecimentoId(1L)
                .nivelRisco(NivelRisco.ALTO)
                .mensagem("Alerta crítico")
                .tipoAlerta(TipoAlerta.ALAGAMENTO)
                .build();

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimentoComTelefone));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta a = invocation.getArgument(0);
            a.setId(30L);
            a.setDataEnvio(LocalDateTime.now());
            return a;
        });
        when(envioAlertaWhatsappRepository.save(any(EnvioAlertaWhatsapp.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(evolutionApiClient.enviarTexto(anyString(), anyString())).thenThrow(new RuntimeException("Gateway offline 503"));

        AlertaResponse response = alertaService.criarAlerta(request);

        assertThat(response).isNotNull();

        ArgumentCaptor<EnvioAlertaWhatsapp> captor = ArgumentCaptor.forClass(EnvioAlertaWhatsapp.class);
        verify(envioAlertaWhatsappRepository, times(2)).save(captor.capture());

        EnvioAlertaWhatsapp envioFinal = captor.getValue();
        assertThat(envioFinal.getStatusEnvio()).isEqualTo(StatusEnvioWhatsapp.FALHA);
        assertThat(envioFinal.getErroMensagem()).contains("Gateway offline 503");
        assertThat(envioFinal.getTentativas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve disparar alertas para todos os estabelecimentos ativos do bairro")
    void deveDispararAlertasParaTodosEstabelecimentosDoBairro() {
        when(estabelecimentoRepository.findByBairroAndAtivoTrue(Bairro.CAMPINA))
                .thenReturn(List.of(estabelecimentoComTelefone, estabelecimentoSemTelefone));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta a = invocation.getArgument(0);
            a.setId(99L);
            a.setDataEnvio(LocalDateTime.now());
            return a;
        });
        when(envioAlertaWhatsappRepository.save(any(EnvioAlertaWhatsapp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<AlertaResponse> respostas = alertaService.dispararAlertaBairro(
                Bairro.CAMPINA,
                NivelRisco.ALTO,
                "Comunicado geral para a Campina",
                TipoAlerta.PREVENTIVO,
                OrigemAlerta.MANUAL
        );

        assertThat(respostas).hasSize(2);
        verify(alertaRepository, times(2)).save(any(Alerta.class));
        verify(evolutionApiClient, times(1)).enviarTexto(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve listar todos os alertas ordenados por data de envio decrescente")
    void deveListarTodosAlertas() {
        Alerta a = new Alerta();
        a.setId(1L);
        a.setEstabelecimento(estabelecimentoComTelefone);
        a.setNivelRisco(NivelRisco.BAIXO);
        a.setMensagem("Tranquilo");
        a.setTipoAlerta(TipoAlerta.ALAGAMENTO);
        a.setDataEnvio(LocalDateTime.now());

        when(alertaRepository.findAll(any(Sort.class))).thenReturn(List.of(a));

        List<AlertaResponse> lista = alertaService.listarTodos();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve buscar alerta por ID com sucesso")
    void deveBuscarAlertaPorId() {
        Alerta a = new Alerta();
        a.setId(5L);
        a.setEstabelecimento(estabelecimentoComTelefone);
        a.setNivelRisco(NivelRisco.MEDIO);
        a.setMensagem("Atenção");
        a.setTipoAlerta(TipoAlerta.ALAGAMENTO);
        a.setDataEnvio(LocalDateTime.now());

        when(alertaRepository.findById(5L)).thenReturn(Optional.of(a));

        AlertaResponse response = alertaService.buscarPorId(5L);

    }

    @Test
    @DisplayName("Deve lançar exceção quando o alerta não for encontrado")
    void deveLancarExcecaoQuandoAlertaNaoExistir() {
        when(alertaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.buscarPorId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alerta não encontrado");
    }

    @Test
    @DisplayName("Deve listar envios de WhatsApp vinculados a um alerta")
    void deveListarEnviosWhatsappPorAlerta() {
        EnvioAlertaWhatsapp envio = EnvioAlertaWhatsapp.builder()
                .id(1L)
                .telefoneDestinatario("5591987654321")
                .statusEnvio(StatusEnvioWhatsapp.ENVIADO)
                .build();

        when(envioAlertaWhatsappRepository.findByAlertaId(10L)).thenReturn(List.of(envio));

        List<EnvioAlertaWhatsapp> envios = alertaService.listarEnviosWhatsapp(10L);

        assertThat(envios).hasSize(1);
        assertThat(envios.get(0).getStatusEnvio()).isEqualTo(StatusEnvioWhatsapp.ENVIADO);
    }
}
