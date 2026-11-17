package com.marealerta.backend.telemetria.service;

import com.marealerta.backend.telemetria.dto.LeituraTelemetriaRequest;
import com.marealerta.backend.telemetria.dto.LeituraTelemetriaResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaPontoGraficoDTO;
import com.marealerta.backend.telemetria.model.FonteTelemetria;
import com.marealerta.backend.telemetria.model.LeituraTelemetria;
import com.marealerta.backend.telemetria.model.TipoRegistroTelemetria;
import com.marealerta.backend.telemetria.repository.LeituraTelemetriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TelemetriaService {

    public static final String ESTACAO_BELEM_CODIGO = "BELEM-CENTRO";
    public static final String ESTACAO_BELEM_NOME = "Estação Ponto de Referência Belém";
    public static final ZoneId BELEM_ZONE = ZoneId.of("America/Belem");

    private final LeituraTelemetriaRepository repository;
    private final OpenMeteoClient openMeteoClient;

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

        OpenMeteoClient.OpenMeteoMarineResponse marineResponse = openMeteoClient.buscarDadosMarinhos(null, null);

        var current = response.current();
        LocalDateTime dataHoraAtual = LocalDateTime.parse(current.time());

        BigDecimal mareAtual = (marineResponse != null && marineResponse.current() != null && marineResponse.current().seaLevelHeightMsl() != null)
                ? marineResponse.current().seaLevelHeightMsl()
                : BigDecimal.ZERO;

        LeituraTelemetria leituraAtual = repository.findByEstacaoCodigoAndFonteAndTipoRegistroAndDataHora(
                ESTACAO_BELEM_CODIGO, FonteTelemetria.OPEN_METEO, TipoRegistroTelemetria.OBSERVADO, dataHoraAtual)
                .orElseGet(() -> LeituraTelemetria.builder()
                        .estacaoCodigo(ESTACAO_BELEM_CODIGO)
                        .estacaoNome(ESTACAO_BELEM_NOME)
                        .fonte(FonteTelemetria.OPEN_METEO)
                        .tipoRegistro(TipoRegistroTelemetria.OBSERVADO)
                        .dataHora(dataHoraAtual)
                        .build());

        leituraAtual.setNivelMare(mareAtual);
        leituraAtual.setPrecipitacao(current.precipitacao());
        leituraAtual.setTemperatura(current.temperatura());
        leituraAtual.setVentoVelocidade(current.ventoVelocidade());
        leituraAtual.setVentoDirecao(current.ventoDirecao());
        leituraAtual.setProbabilidadeChuva(resolveCurrentRainProbability(response.hourly(), current.time()));

        LeituraTelemetria salva = repository.save(leituraAtual);
        sincronizarPrevisoesHorarias(response.hourly(), marineResponse != null ? marineResponse.hourly() : null);

        return LeituraTelemetriaResponse.fromEntity(salva);
    }

    @Transactional
    public TelemetriaDashboardResponse obterDashboard() {
        LeituraTelemetria ultimaLeitura = repository.findTop1ByEstacaoCodigoAndTipoRegistroOrderByDataHoraDesc(
                        ESTACAO_BELEM_CODIGO, TipoRegistroTelemetria.OBSERVADO)
                .orElseGet(() -> repository.findTop1ByOrderByDataHoraDesc()
                        .orElseGet(() -> {
                            sincronizarOpenMeteo();
                            return repository.findTop1ByOrderByDataHoraDesc().orElseThrow();
                        }));

        LocalDateTime agoraBelem = LocalDateTime.now(BELEM_ZONE);
        if (ultimaLeitura.getDataHora() != null && ultimaLeitura.getDataHora().isBefore(agoraBelem.minusMinutes(30))) {
            try {
                sincronizarOpenMeteo();
                ultimaLeitura = repository.findTop1ByEstacaoCodigoAndTipoRegistroOrderByDataHoraDesc(
                                ESTACAO_BELEM_CODIGO, TipoRegistroTelemetria.OBSERVADO)
                        .orElse(ultimaLeitura);
            } catch (Exception ignored) {
            }
        }

        List<LeituraTelemetria> proximasPrevisoes = repository.findByTipoRegistroAndDataHoraBetweenOrderByDataHoraAsc(
                TipoRegistroTelemetria.PREVISAO, ultimaLeitura.getDataHora(), ultimaLeitura.getDataHora().plusHours(3));

        String tendencia = "ESTAVEL";
        if (!proximasPrevisoes.isEmpty() && ultimaLeitura.getNivelMare() != null && proximasPrevisoes.get(0).getNivelMare() != null) {
            double diferenca = proximasPrevisoes.get(0).getNivelMare().subtract(ultimaLeitura.getNivelMare()).doubleValue();
            if (diferenca > 0.05) {
                tendencia = "SUBINDO";
            } else if (diferenca < -0.05) {
                tendencia = "DESCENDO";
            }
        }

        List<LeituraTelemetria> janela12h = repository.findByTipoRegistroAndDataHoraBetweenOrderByDataHoraAsc(
                TipoRegistroTelemetria.PREVISAO, ultimaLeitura.getDataHora(), ultimaLeitura.getDataHora().plusHours(12));

        Optional<LeituraTelemetria> preamarLeitura = janela12h.stream()
                .filter(l -> l.getNivelMare() != null)
                .max(Comparator.comparing(LeituraTelemetria::getNivelMare));

        LocalDateTime preamarHorario = preamarLeitura
                .map(LeituraTelemetria::getDataHora)
                .orElse(ultimaLeitura.getDataHora());

        BigDecimal nivelArredondado = ultimaLeitura.getNivelMare() != null
                ? ultimaLeitura.getNivelMare().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal preamarNivelPrevisto = preamarLeitura
                .map(l -> l.getNivelMare().setScale(2, RoundingMode.HALF_UP))
                .orElse(nivelArredondado);
        BigDecimal chuvaArredondada = ultimaLeitura.getPrecipitacao() != null
                ? ultimaLeitura.getPrecipitacao().setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal tempArredondada = ultimaLeitura.getTemperatura() != null
                ? ultimaLeitura.getTemperatura().setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal ventoArredondado = ultimaLeitura.getVentoVelocidade() != null
                ? ultimaLeitura.getVentoVelocidade().setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String preamarFormatado = preamarHorario != null ? preamarHorario.format(horaFormatter) : null;

        Integer probChuvaAtual = ultimaLeitura.getProbabilidadeChuva();
        if (probChuvaAtual == null && !proximasPrevisoes.isEmpty()) {
            probChuvaAtual = proximasPrevisoes.get(0).getProbabilidadeChuva();
        }

        return TelemetriaDashboardResponse.builder()
                .estacaoCodigo(ultimaLeitura.getEstacaoCodigo())
                .estacaoNome(ultimaLeitura.getEstacaoNome())
                .nivelAtual(nivelArredondado)
                .nivelFormatado(String.format(Locale.US, "%.2f m", nivelArredondado.doubleValue()))
                .tendencia(tendencia)
                .preamarHorario(preamarHorario)
                .preamarHorarioFormatado(preamarFormatado)
                .preamarNivelPrevisto(preamarNivelPrevisto)
                .probabilidadeChuvaAtual(probChuvaAtual)
                .chuvaAtual(chuvaArredondada)
                .chuvaFormatada(String.format(Locale.US, "%.1f mm/h", chuvaArredondada.doubleValue()))
                .temperatura(tempArredondada)
                .temperaturaFormatada(String.format(Locale.US, "%.1f °C", tempArredondada.doubleValue()))
                .ventoVelocidade(ventoArredondado)
                .ventoFormatado(String.format(Locale.US, "%.1f km/h", ventoArredondado.doubleValue()))
                .ventoDirecao(ultimaLeitura.getVentoDirecao())
                .nivelRisco(calcularNivelRisco(nivelArredondado, chuvaArredondada))
                .dataHora(ultimaLeitura.getDataHora())
                .unidadeNivel("m")
                .unidadeChuva("mm/h")
                .unidadeTemperatura("°C")
                .unidadeVento("km/h")
                .build();
    }

    @Transactional(readOnly = true)
    public List<LeituraTelemetria> obterPrevisoesProximasHoras(int horas) {
        LocalDateTime agora = LocalDateTime.now(BELEM_ZONE);
        return repository.findByTipoRegistroAndDataHoraBetweenOrderByDataHoraAsc(
                TipoRegistroTelemetria.PREVISAO, agora, agora.plusHours(Math.max(1, horas)));
    }

    @Transactional(readOnly = true)
    public List<TelemetriaPontoGraficoDTO> obterPontosGrafico() {
        LocalDateTime agoraBelem = LocalDateTime.now(BELEM_ZONE);
        LocalDateTime inicio = agoraBelem.minusHours(12);
        LocalDateTime fim = agoraBelem.plusHours(12);

        List<LeituraTelemetria> leituras = repository.findByEstacaoCodigoAndDataHoraBetweenOrderByDataHoraAsc(
                ESTACAO_BELEM_CODIGO, inicio, fim);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return leituras.stream()
                .map(l -> TelemetriaPontoGraficoDTO.builder()
                        .time(l.getDataHora().format(formatter))
                        .hour(l.getDataHora().getHour())
                        .dataHora(l.getDataHora())
                        .level(l.getNivelMare() != null ? l.getNivelMare().setScale(2, RoundingMode.HALF_UP) : null)
                        .rain(l.getPrecipitacao() != null ? l.getPrecipitacao().setScale(1, RoundingMode.HALF_UP) : null)
                        .temperature(l.getTemperatura() != null ? l.getTemperatura().setScale(1, RoundingMode.HALF_UP) : null)
                        .wind(l.getVentoVelocidade() != null ? l.getVentoVelocidade().setScale(1, RoundingMode.HALF_UP) : null)
                        .rainProbability(l.getProbabilidadeChuva())
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

    private void sincronizarPrevisoesHorarias(OpenMeteoClient.HourlyData hourly, OpenMeteoClient.MarineHourly marineHourly) {
        if (hourly == null || hourly.time() == null) return;

        Map<String, BigDecimal> marePorHorario = new HashMap<>();
        if (marineHourly != null && marineHourly.time() != null && marineHourly.seaLevelHeightMsl() != null) {
            for (int j = 0; j < Math.min(marineHourly.time().size(), marineHourly.seaLevelHeightMsl().size()); j++) {
                marePorHorario.put(marineHourly.time().get(j), marineHourly.seaLevelHeightMsl().get(j));
            }
        }

        LocalDateTime agora = LocalDateTime.now(BELEM_ZONE);
        List<LeituraTelemetria> previsoes = new ArrayList<>();
        for (int i = 0; i < hourly.time().size(); i++) {
            String timeStr = hourly.time().get(i);
            LocalDateTime dt = LocalDateTime.parse(timeStr);
            if (dt.isAfter(agora)) {
                BigDecimal marePrevisao = marePorHorario.getOrDefault(timeStr, BigDecimal.ZERO);
                LeituraTelemetria prev = repository.findByEstacaoCodigoAndFonteAndTipoRegistroAndDataHora(
                        ESTACAO_BELEM_CODIGO, FonteTelemetria.OPEN_METEO, TipoRegistroTelemetria.PREVISAO, dt)
                        .orElseGet(() -> LeituraTelemetria.builder()
                                .estacaoCodigo(ESTACAO_BELEM_CODIGO)
                                .estacaoNome(ESTACAO_BELEM_NOME)
                                .fonte(FonteTelemetria.OPEN_METEO)
                                .tipoRegistro(TipoRegistroTelemetria.PREVISAO)
                                .dataHora(dt)
                                .build());

                prev.setNivelMare(marePrevisao);
                prev.setPrecipitacao(hourly.precipitacao() != null && i < hourly.precipitacao().size() ? hourly.precipitacao().get(i) : BigDecimal.ZERO);
                prev.setTemperatura(hourly.temperatura() != null && i < hourly.temperatura().size() ? hourly.temperatura().get(i) : BigDecimal.ZERO);
                prev.setVentoVelocidade(hourly.ventoVelocidade() != null && i < hourly.ventoVelocidade().size() ? hourly.ventoVelocidade().get(i) : BigDecimal.ZERO);
                prev.setVentoDirecao(hourly.ventoDirecao() != null && i < hourly.ventoDirecao().size() ? hourly.ventoDirecao().get(i) : BigDecimal.ZERO);
                prev.setProbabilidadeChuva(extractProbabilityAt(hourly.probabilidadePrecipitacao(), i));

                previsoes.add(prev);
            }
        }
        repository.saveAll(previsoes);
    }

    private Integer resolveCurrentRainProbability(OpenMeteoClient.HourlyData hourly, String currentTimeStr) {
        if (hourly == null || hourly.probabilidadePrecipitacao() == null || hourly.probabilidadePrecipitacao().isEmpty()) {
            return null;
        }
        if (hourly.time() != null && currentTimeStr != null && currentTimeStr.length() >= 13) {
            String prefix = currentTimeStr.substring(0, 13);
            for (int i = 0; i < hourly.time().size(); i++) {
                String slot = hourly.time().get(i);
                if (slot != null && slot.startsWith(prefix)) {
                    return extractProbabilityAt(hourly.probabilidadePrecipitacao(), i);
                }
            }
        }
        return extractProbabilityAt(hourly.probabilidadePrecipitacao(), 0);
    }

    private Integer extractProbabilityAt(List<BigDecimal> probs, int index) {
        if (probs == null || index < 0 || index >= probs.size() || probs.get(index) == null) {
            return null;
        }
        int rounded = (int) Math.round(probs.get(index).doubleValue());
        return Math.max(0, Math.min(100, rounded));
    }

    private String calcularNivelRisco(BigDecimal nivelMare, BigDecimal chuva) {
        double mareVal = nivelMare != null ? nivelMare.doubleValue() : 0.0;
        double chuvaVal = chuva != null ? chuva.doubleValue() : 0.0;

        if (mareVal >= 3.2 || chuvaVal >= 30.0) return "ALTO";
        if (mareVal >= 2.8 || chuvaVal >= 10.0) return "MEDIO";
        return "BAIXO";
    }
}
