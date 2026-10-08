package com.marealerta.backend.telemetria.service;

import com.marealerta.backend.telemetria.service.OpenMeteoClient;
import com.marealerta.backend.telemetria.dto.LeituraTelemetriaRequest;
import com.marealerta.backend.telemetria.dto.LeituraTelemetriaResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.service.OpenMeteoClient;
import com.marealerta.backend.telemetria.dto.LeituraTelemetriaRequest;
import com.marealerta.backend.telemetria.dto.LeituraTelemetriaResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaPontoGraficoDTO;
import com.marealerta.backend.telemetria.model.LeituraTelemetria;
import com.marealerta.backend.telemetria.model.TipoRegistroTelemetria;
import com.marealerta.backend.telemetria.repository.LeituraTelemetriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TelemetriaService {

    public static final String ESTACAO_BELEM_CODIGO = "BELEM-CENTRO";
    public static final String ESTACAO_BELEM_NOME = "Estação Ponto de Referência Belém";

    private final LeituraTelemetriaRepository repository;
    private final OpenMeteoClient openMeteoClient;

    public TelemetriaService(LeituraTelemetriaRepository repository, OpenMeteoClient openMeteoClient) {
        this.repository = repository;
        this.openMeteoClient = openMeteoClient;
    }

    @Transactional
    public LeituraTelemetriaResponse salvarManual(LeituraTelemetriaRequest request) {
        LeituraTelemetria leitura = LeituraTelemetria.builder()
                .estacaoCodigo(request.getEstacaoCodigo())
                .estacaoNome(request.getEstacaoNome() != null ? request.getEstacaoNome() : ESTACAO_BELEM_NOME)
                .tipoRegistro(request.getTipoRegistro())
                .nivelMare(request.getNivelMare())
                .precipitacao(request.getPrecipitacao())
                .temperatura(request.getTemperatura())
                .ventoVelocidade(request.getVentoVelocidade())
                .ventoDirecao(request.getVentoDirecao())
                .dataHora(request.getDataHora())
                .build();

        return LeituraTelemetriaResponse.fromEntity(repository.save(leitura));
    }

    @Transactional
    public LeituraTelemetriaResponse sincronizarOpenMeteo() {
        OpenMeteoClient.OpenMeteoResponse response = openMeteoClient.buscarDadosAtuaisEPrevisao();

        if (response == null || response.current() == null) {
            throw new RuntimeException("Falha ao obter dados meteorológicos da API Open-Meteo.");
        }

        var current = response.current();
        LocalDateTime dataHoraAtual = LocalDateTime.parse(current.time());

        LeituraTelemetria leituraAtual = LeituraTelemetria.builder()
                .estacaoCodigo(ESTACAO_BELEM_CODIGO)
                .estacaoNome(ESTACAO_BELEM_NOME)
                .tipoRegistro(TipoRegistroTelemetria.OBSERVADO)
                .nivelMare(estimarNivelMare(dataHoraAtual))
                .precipitacao(current.precipitacao())
                .temperatura(current.temperatura())
                .ventoVelocidade(current.ventoVelocidade())
                .ventoDirecao(current.ventoDirecao())
                .dataHora(dataHoraAtual)
                .build();

        LeituraTelemetria salva = repository.save(leituraAtual);
        sincronizarPrevisoesHorarias(response.hourly());

        return LeituraTelemetriaResponse.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public TelemetriaDashboardResponse obterDashboard() {
        LeituraTelemetria ultimaLeitura = repository.findTop1ByEstacaoCodigoAndTipoRegistroOrderByDataHoraDesc(
                        ESTACAO_BELEM_CODIGO, TipoRegistroTelemetria.OBSERVADO)
                .orElseGet(() -> repository.findTop1ByOrderByDataHoraDesc()
                        .orElseGet(() -> {
                            sincronizarOpenMeteo();
                            return repository.findTop1ByOrderByDataHoraDesc().orElseThrow();
                        }));

        return TelemetriaDashboardResponse.builder()
                .estacaoCodigo(ultimaLeitura.getEstacaoCodigo())
                .estacaoNome(ultimaLeitura.getEstacaoNome())
                .nivelAtual(ultimaLeitura.getNivelMare())
                .tendencia("SUBINDO")
                .preamarHorario(ultimaLeitura.getDataHora().plusHours(2))
                .chuvaAtual(ultimaLeitura.getPrecipitacao())
                .temperatura(ultimaLeitura.getTemperatura())
                .ventoVelocidade(ultimaLeitura.getVentoVelocidade())
                .ventoDirecao(ultimaLeitura.getVentoDirecao())
                .nivelRisco(calcularNivelRisco(ultimaLeitura.getNivelMare(), ultimaLeitura.getPrecipitacao()))
                .dataHora(ultimaLeitura.getDataHora())
                .unidadeNivel("m")
                .unidadeChuva("mm/h")
                .unidadeTemperatura("°C")
                .unidadeVento("km/h")
                .build();
    }

    @Transactional(readOnly = true)
    public List<TelemetriaPontoGraficoDTO> obterPontosGrafico() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(12);
        LocalDateTime fim = LocalDateTime.now().plusHours(12);

        List<LeituraTelemetria> leituras = repository.findByEstacaoCodigoAndDataHoraBetweenOrderByDataHoraAsc(
                ESTACAO_BELEM_CODIGO, inicio, fim);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return leituras.stream()
                .map(l -> TelemetriaPontoGraficoDTO.builder()
                        .time(l.getDataHora().format(formatter))
                        .dataHora(l.getDataHora())
                        .level(l.getNivelMare())
                        .rain(l.getPrecipitacao())
                        .temperature(l.getTemperatura())
                        .wind(l.getVentoVelocidade())
                        .tipoRegistro(l.getTipoRegistro() != null ? l.getTipoRegistro().name() : "OBSERVADO")
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeituraTelemetriaResponse> obterHistoricoRecente(String estacaoCodigo) {
        String codigo = estacaoCodigo != null ? estacaoCodigo : ESTACAO_BELEM_CODIGO;
        return repository.findTop10ByEstacaoCodigoOrderByDataHoraDesc(codigo)
                .stream()
                .map(LeituraTelemetriaResponse::fromEntity)
                .toList();
    }

    private void sincronizarPrevisoesHorarias(OpenMeteoClient.HourlyData hourly) {
        if (hourly == null || hourly.time() == null) return;

        List<LeituraTelemetria> previsoes = new ArrayList<>();
        for (int i = 0; i < hourly.time().size(); i++) {
            LocalDateTime dt = LocalDateTime.parse(hourly.time().get(i));
            if (dt.isAfter(LocalDateTime.now())) {
                previsoes.add(LeituraTelemetria.builder()
                        .estacaoCodigo(ESTACAO_BELEM_CODIGO)
                        .estacaoNome(ESTACAO_BELEM_NOME)
                        .tipoRegistro(TipoRegistroTelemetria.PREVISAO)
                        .nivelMare(estimarNivelMare(dt))
                        .precipitacao(hourly.precipitacao().get(i))
                        .temperatura(hourly.temperatura().get(i))
                        .ventoVelocidade(hourly.ventoVelocidade().get(i))
                        .ventoDirecao(hourly.ventoDirecao().get(i))
                        .dataHora(dt)
                        .build());
            }
        }
        repository.saveAll(previsoes);
    }

    private BigDecimal estimarNivelMare(LocalDateTime dt) {
        double hora = dt.getHour() + (dt.getMinute() / 60.0);
        double nivelSimulado = 2.0 + 1.2 * Math.sin((hora / 12.0) * 2 * Math.PI);
        return BigDecimal.valueOf(nivelSimulado).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String calcularNivelRisco(BigDecimal nivelMare, BigDecimal chuva) {
        double mareVal = nivelMare != null ? nivelMare.doubleValue() : 0.0;
        double chuvaVal = chuva != null ? chuva.doubleValue() : 0.0;

        if (mareVal >= 3.2 || chuvaVal >= 30.0) return "ALTO";
        if (mareVal >= 2.8 || chuvaVal >= 10.0) return "MEDIO";
        return "BAIXO";
    }
}