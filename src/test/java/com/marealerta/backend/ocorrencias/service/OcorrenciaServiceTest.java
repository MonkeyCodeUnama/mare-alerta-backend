package com.marealerta.backend.ocorrencias.service;

import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaFotoResponse;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaRequest;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaResponse;
import com.marealerta.backend.ocorrencias.model.NivelAlagamento;
import com.marealerta.backend.ocorrencias.model.Ocorrencia;
import com.marealerta.backend.ocorrencias.model.OcorrenciaFoto;
import com.marealerta.backend.ocorrencias.model.StatusOcorrencia;
import com.marealerta.backend.ocorrencias.repository.OcorrenciaFotoRepository;
import com.marealerta.backend.ocorrencias.repository.OcorrenciaRepository;
import com.marealerta.backend.usuario.model.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcorrenciaServiceTest {

    @Mock
    private OcorrenciaRepository ocorrenciaRepository;

    @Mock
    private OcorrenciaFotoRepository ocorrenciaFotoRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @InjectMocks
    private OcorrenciaService ocorrenciaService;

    @TempDir
    Path tempDir;

    private Usuario usuario;
    private Estabelecimento estabelecimento;
    private OcorrenciaRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ocorrenciaService, "ocorrenciaDir", tempDir.toString());

        estabelecimento = Estabelecimento.builder()
                .id(10L)
                .nome("Mercado Central")
                .build();

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@example.com");
        usuario.setEstabelecimento(estabelecimento);

        request = OcorrenciaRequest.builder()
                .titulo("Alagamento na Entrada")
                .descricao("Água subiu cerca de 20cm após chuva torrencial")
                .status(StatusOcorrencia.ENVIADA)
                .nivelAlagamento(NivelAlagamento.MODERADO)
                .dataOcorrencia(LocalDate.now())
                .horaInicio(LocalTime.of(14, 0))
                .horaFim(LocalTime.of(16, 30))
                .faixaLaminaAgua("15-30 cm")
                .categoriasDano("ESTRUTURAL,ESTOQUE")
                .valorEstimadoPrejuizo(BigDecimal.valueOf(1500.00))
                .horasFechado(3)
                .tamanhoEquipe(2)
                .build();
    }

    @Test
    @DisplayName("Deve registrar ocorrência com protocolo no padrão OC-{estabId}-{quant} e salvar com sucesso")
    void deveRegistrarOcorrenciaComSucesso() {
        when(estabelecimentoRepository.findById(10L)).thenReturn(Optional.of(estabelecimento));
        when(ocorrenciaRepository.countByEstabelecimentoId(10L)).thenReturn(2L);
        when(ocorrenciaRepository.save(any(Ocorrencia.class))).thenAnswer(invocation -> {
            Ocorrencia saved = invocation.getArgument(0);
            return Ocorrencia.builder()
                    .id(100L)
                    .titulo(saved.getTitulo())
                    .descricao(saved.getDescricao())
                    .status(saved.getStatus())
                    .nivelAlagamento(saved.getNivelAlagamento())
                    .dataOcorrencia(saved.getDataOcorrencia())
                    .horaInicio(saved.getHoraInicio())
                    .horaFim(saved.getHoraFim())
                    .faixaLaminaAgua(saved.getFaixaLaminaAgua())
                    .categoriasDano(saved.getCategoriasDano())
                    .valorEstimadoPrejuizo(saved.getValorEstimadoPrejuizo())
                    .horasFechado(saved.getHorasFechado())
                    .tamanhoEquipe(saved.getTamanhoEquipe())
                    .estabelecimento(saved.getEstabelecimento())
                    .protocolo(saved.getProtocolo())
                    .build();
        });

        OcorrenciaResponse response = ocorrenciaService.registrarO(usuario, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitulo()).isEqualTo("Alagamento na Entrada");
        assertThat(response.getProtocolo()).isEqualTo("OC-10-3");

        verify(estabelecimentoRepository).findById(10L);
        verify(ocorrenciaRepository).countByEstabelecimentoId(10L);
        verify(ocorrenciaRepository).save(any(Ocorrencia.class));
    }

    @Test
    @DisplayName("Deve lançar ResponseStatusException (UNAUTHORIZED) quando usuário for nulo")
    void deveLancarExcecaoQuandoUsuarioNaoAutenticado() {
        assertThatThrownBy(() -> ocorrenciaService.registrarO(null, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("usuario nao autenticado");

        verifyNoInteractions(estabelecimentoRepository, ocorrenciaRepository);
    }

    @Test
    @DisplayName("Deve lançar ResponseStatusException (BAD_REQUEST) quando usuário não possuir estabelecimento vinculado")
    void deveLancarExcecaoQuandoUsuarioSemEstabelecimento() {
        Usuario semEstab = new Usuario();
        semEstab.setId(2L);
        semEstab.setEmail("noestab@example.com");

        assertThatThrownBy(() -> ocorrenciaService.registrarO(semEstab, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("usuario nao possui estabelecimento vinculado");

        verifyNoInteractions(estabelecimentoRepository, ocorrenciaRepository);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando estabelecimento não existir")
    void deveLancarExcecaoQuandoEstabelecimentoNaoEncontrado() {
        when(estabelecimentoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ocorrenciaService.registrarO(usuario, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("estabelecimento nao encontrado");

        verify(estabelecimentoRepository).findById(10L);
        verify(ocorrenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar ocorrências do usuário ordenadas por data e hora")
    void deveListarOcorrenciasDoUsuario() {
        Ocorrencia oc = Ocorrencia.builder()
                .id(1L)
                .titulo("Ocorrencia 1")
                .estabelecimento(estabelecimento)
                .status(StatusOcorrencia.ENVIADA)
                .protocolo("OC-10-1")
                .dataOcorrencia(LocalDate.now())
                .horaInicio(LocalTime.of(10, 0))
                .build();

        when(ocorrenciaRepository.findByEstabelecimentoIdOrderByDataOcorrenciaDescHoraInicioDesc(10L))
                .thenReturn(List.of(oc));

        List<OcorrenciaResponse> lista = ocorrenciaService.listarPorUsuario(usuario, null);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getProtocolo()).isEqualTo("OC-10-1");
        verify(ocorrenciaRepository).findByEstabelecimentoIdOrderByDataOcorrenciaDescHoraInicioDesc(10L);
    }

    @Test
    @DisplayName("Deve listar ocorrências do usuário filtradas por status")
    void deveListarOcorrenciasDoUsuarioPorStatus() {
        Ocorrencia oc = Ocorrencia.builder()
                .id(2L)
                .titulo("Ocorrencia Em Analise")
                .estabelecimento(estabelecimento)
                .status(StatusOcorrencia.EM_ANALISE)
                .protocolo("OC-10-2")
                .dataOcorrencia(LocalDate.now())
                .horaInicio(LocalTime.of(12, 0))
                .build();

        when(ocorrenciaRepository.findByEstabelecimentoIdAndStatus(10L, StatusOcorrencia.EM_ANALISE))
                .thenReturn(List.of(oc));

        List<OcorrenciaResponse> lista = ocorrenciaService.listarPorUsuario(usuario, StatusOcorrencia.EM_ANALISE);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getStatus()).isEqualTo(StatusOcorrencia.EM_ANALISE);
        verify(ocorrenciaRepository).findByEstabelecimentoIdAndStatus(10L, StatusOcorrencia.EM_ANALISE);
    }

    @Test
    @DisplayName("Deve buscar ocorrência por ID com sucesso")
    void deveBuscarOcorrenciaPorIdComSucesso() {
        Ocorrencia oc = Ocorrencia.builder()
                .id(50L)
                .titulo("Ocorrencia 50")
                .estabelecimento(estabelecimento)
                .protocolo("OC-10-50")
                .build();

        when(ocorrenciaRepository.findById(50L)).thenReturn(Optional.of(oc));

        OcorrenciaResponse response = ocorrenciaService.buscarPorId(usuario, 50L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getProtocolo()).isEqualTo("OC-10-50");
    }

    @Test
    @DisplayName("Deve rejeitar busca por ID se pertencer a outro estabelecimento")
    void deveRejeitarBuscarPorIdDeOutroEstabelecimento() {
        Estabelecimento outroEstab = Estabelecimento.builder().id(999L).nome("Outro").build();
        Ocorrencia ocOutro = Ocorrencia.builder()
                .id(50L)
                .estabelecimento(outroEstab)
                .build();

        when(ocorrenciaRepository.findById(50L)).thenReturn(Optional.of(ocOutro));

        assertThatThrownBy(() -> ocorrenciaService.buscarPorId(usuario, 50L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("usuario nao autorizado a visualizar esta ocorrencia");
    }

    @Test
    @DisplayName("Deve buscar ocorrência por protocolo com sucesso")
    void deveBuscarOcorrenciaPorProtocoloComSucesso() {
        Ocorrencia oc = Ocorrencia.builder()
                .id(70L)
                .titulo("Ocorrencia 70")
                .estabelecimento(estabelecimento)
                .protocolo("OC-10-70")
                .build();

        when(ocorrenciaRepository.findByProtocolo("OC-10-70")).thenReturn(Optional.of(oc));

        OcorrenciaResponse response = ocorrenciaService.buscarPorProtocolo("OC-10-70");

        assertThat(response).isNotNull();
        assertThat(response.getProtocolo()).isEqualTo("OC-10-70");
    }

    @Test
    @DisplayName("Deve adicionar foto na ocorrência com sucesso")
    void deveAdicionarFotoComSucesso() throws IOException {
        Ocorrencia ocorrencia = Ocorrencia.builder()
                .id(50L)
                .estabelecimento(estabelecimento)
                .protocolo("OC-10-1")
                .build();

        when(ocorrenciaRepository.findById(50L)).thenReturn(Optional.of(ocorrencia));
        when(ocorrenciaFotoRepository.save(any(OcorrenciaFoto.class))).thenAnswer(invocation -> {
            OcorrenciaFoto f = invocation.getArgument(0);
            return OcorrenciaFoto.builder()
                    .id(1L)
                    .nomeArquivo(f.getNomeArquivo())
                    .urlFoto(f.getUrlFoto())
                    .tamanhoBytes(f.getTamanhoBytes())
                    .contentType(f.getContentType())
                    .ocorrencia(f.getOcorrencia())
                    .build();
        });

        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "foto1.jpg", "image/jpeg", "dados da imagem de teste".getBytes()
        );

        OcorrenciaFotoResponse fotoResponse = ocorrenciaService.adicionarFoto(usuario, 50L, file);

        assertThat(fotoResponse).isNotNull();
        assertThat(fotoResponse.getNomeArquivo()).isEqualTo("foto1.jpg");
        assertThat(fotoResponse.getUrlFoto()).contains("uploads/ocorrencias/");

        verify(ocorrenciaRepository).findById(50L);
        verify(ocorrenciaFotoRepository).save(any(OcorrenciaFoto.class));
    }

    @Test
    @DisplayName("Deve rejeitar foto quando ocorrência pertencer a outro estabelecimento")
    void deveRejeitarFotoQuandoOcorrenciaNaoPertencerAoUsuario() {
        Estabelecimento outroEstab = Estabelecimento.builder().id(999L).nome("Outro").build();
        Ocorrencia ocorrenciaOutro = Ocorrencia.builder()
                .id(50L)
                .estabelecimento(outroEstab)
                .build();

        when(ocorrenciaRepository.findById(50L)).thenReturn(Optional.of(outroEstab != null ? ocorrenciaOutro : null));

        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        assertThatThrownBy(() -> ocorrenciaService.adicionarFoto(usuario, 50L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("usuario nao autorizado");

        verify(ocorrenciaFotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar foto vazia com erro 400")
    void deveRejeitarFotoVazia() {
        MockMultipartFile emptyFile = new MockMultipartFile("arquivo", "vazio.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> ocorrenciaService.adicionarFoto(usuario, 50L, emptyFile))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("arquivo de foto invalido ou vazio");

        verifyNoInteractions(ocorrenciaRepository, ocorrenciaFotoRepository);
    }
}
