package com.marealerta.backend.alerta.service;

import com.marealerta.backend.alerta.AlertaService;
import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.model.*;
import com.marealerta.backend.alerta.repository.AlertaRepository;
import com.marealerta.backend.alerta.repository.EnvioAlertaWhatsappRepository;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private EnvioAlertaWhatsappRepository envioWhatsappRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private AlertaService alertaService;

    @Test
    void deveCriarAlertaEEnviarWhatsappComSucesso() throws BadRequestException {
        Long estabelecimentoId = 1L;
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(estabelecimentoId);
        estabelecimento.setNome("Comércio do Zé");
        estabelecimento.setTelefone("91999999999");

        AlertaRequest request = new AlertaRequest();
        request.setEstabelecimentoId(estabelecimentoId);
        request.setNivelRisco(NivelRisco.ALTO);
        request.setMensagem("Alerta de maré alta!");
        request.setTipoAlerta(TipoAlerta.MARE_ALTA);
        request.setOrigemAlerta(OrigemAlerta.AUTOMATICO);

        when(estabelecimentoRepository.findById(estabelecimentoId))
                .thenReturn(Optional.of(estabelecimento));

        // Simula o salvamento retornando o próprio objeto passado
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(envioWhatsappRepository.save(any(EnvioAlertaWhatsapp.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertaResponse response = alertaService.enviarAlerta(request);

        assertNotNull(response);
        assertEquals("Comércio do Zé", response.getEstabelecimentoNome());
        assertEquals(NivelRisco.ALTO, response.getNivelRisco());

        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }
}