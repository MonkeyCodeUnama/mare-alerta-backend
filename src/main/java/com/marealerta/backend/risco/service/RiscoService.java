package com.marealerta.backend.risco.service;

import com.marealerta.backend.alerta.model.NivelRisco;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.risco.dto.BairroCatalogoDTO;
import com.marealerta.backend.risco.dto.PrevisaoMlRequest;
import com.marealerta.backend.risco.dto.PrevisaoMlResponse;
import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.risco.model.PrevisaoChuvaMl;
import com.marealerta.backend.risco.model.StatusQualidadeMl;
import com.marealerta.backend.risco.repository.PrevisaoChuvaMlRepository;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.model.LeituraTelemetria;
import com.marealerta.backend.telemetria.service.OpenMeteoClient;
import com.marealerta.backend.telemetria.service.TelemetriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiscoService {

    private final MlApiCliente mlApiClient;
    private final PrevisaoChuvaMlRepository previsaoChuvaMlRepository;
    private final TelemetriaService telemetriaService;
    private final OpenMeteoClient openMeteoClient;

    public List<BairroCatalogoDTO> listarCatalogoBairros() {
        return Arrays.stream(Bairro.values())
                .map(BairroCatalogoDTO::fromEnum)
                .toList();
    }

    @Transactional
    public RiscoResponse processarEObterRiscoParaLocalidade(String localidade) {
        Bairro bairro = Bairro.fromString(localidade);
        double lat = (bairro != null && bairro.getLatitude() != null) ? bairro.getLatitude() : -1.4558;
        double lon = (bairro != null && bairro.getLongitude() != null) ? bairro.getLongitude() : -48.5044;

        TelemetriaDashboardResponse telemetria = telemetriaService.obterDashboard();
        OpenMeteoClient.GfsFeaturesSnapshot gfsSnapshot = openMeteoClient.buscarFeaturesGfsAtuais(lat, lon);

        PrevisaoMlRequest request = new PrevisaoMlRequest(
                "rain-hourly-v1",
                BigDecimal.valueOf(lat),
                BigDecimal.valueOf(lon),
                gfsSnapshot.featureTime(),
                1,
                gfsSnapshot.features(),
                gfsSnapshot.units()
        );

        return processarEObterRiscoInterno(request, localidade, telemetria, bairro, lat, lon);
    }

    @Transactional
    public RiscoResponse processarEObterRisco(PrevisaoMlRequest request, String localidade, TelemetriaDashboardResponse telemetria) {
        Bairro bairro = Bairro.fromString(localidade);
        double lat = request.latitude() != null ? request.latitude().doubleValue() : -1.4558;
        double lon = request.longitude() != null ? request.longitude().doubleValue() : -48.5044;
        return processarEObterRiscoInterno(request, localidade, telemetria, bairro, lat, lon);
    }

    private RiscoResponse processarEObterRiscoInterno(
            PrevisaoMlRequest request,
            String localidade,
            TelemetriaDashboardResponse telemetria,
            Bairro bairro,
            double lat,
            double lon) {

        if (telemetria == null) {
            telemetria = telemetriaService.obterDashboard();
        }

        PrevisaoMlResponse responseMl;
        StatusQualidadeMl statusQualidade;

        try {
            responseMl = mlApiClient.solicitarPrevisao(request);
            statusQualidade = (responseMl.getQualityStatus() != null) ? responseMl.getQualityStatus() : StatusQualidadeMl.OK;
        } catch (Exception ex) {
            log.warn("Servico de Machine Learning indisponivel ou retornou erro. Acionando fallback heuristico: {}", ex.getMessage());
            responseMl = criarPrevisaoFallback(telemetria);
            statusQualidade = StatusQualidadeMl.DEGRADADO;
        }


        String estacao = (localidade != null && !localidade.isBlank()) ? localidade : "BELEM_CENTRO";
        Instant instanteEmissao = responseMl.getIssuedAt() != null ? responseMl.getIssuedAt() : Instant.now();
        Instant instanteAlvo = responseMl.getTargetTime() != null ? responseMl.getTargetTime() : Instant.now().plus(1, ChronoUnit.HOURS);
        Integer horizonteHoras = responseMl.getHorizonHours() != null ? responseMl.getHorizonHours() : 1;

        Double probMl = responseMl.getRainProbability() != null ? responseMl.getRainProbability() : 0.0;
        CalibrationResult calibration = calibrarProbabilidade(probMl, lat, lon);

        double probArredondada = BigDecimal.valueOf(calibration.probabilidade()).setScale(2, RoundingMode.HALF_UP).doubleValue();
        int probPercentual = (int) Math.round(probArredondada * 100);
        String probFormatada = probPercentual + "%";

        PrevisaoChuvaMl entidade = PrevisaoChuvaMl.builder()
                .estacaoCodigo(estacao)
                .instanteEmissao(instanteEmissao)
                .instanteAlvo(instanteAlvo)
                .horizonteHoras(horizonteHoras)
                .probabilidadeChuva(probArredondada)
                .chuvaEsperada(responseMl.getRainExpected())
                .precipitacaoEstimadaMm(responseMl.getExpectedPrecipitationMm())
                .versaoModelo(responseMl.getModelVersion() != null ? responseMl.getModelVersion() : "v1.0.0")
                .statusQualidade(statusQualidade)
                .criadoEm(Instant.now())
                .build();

        try {
            previsaoChuvaMlRepository.save(entidade);
        } catch (Exception ex) {
            log.warn("Falha ao salvar historico de previsao ML: {}", ex.getMessage());
        }

        List<LeituraTelemetria> previsoes12h = Optional.ofNullable(telemetriaService.obterPrevisoesProximasHoras(12))
                .orElse(List.of());

        BigDecimal nivelMareAtual = telemetria.getNivelAtual() != null
                ? telemetria.getNivelAtual().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Optional<LeituraTelemetria> picoPrevisao = previsoes12h.stream()
                .filter(l -> l.getNivelMare() != null && l.getDataHora() != null)
                .max(Comparator.comparing(LeituraTelemetria::getNivelMare));

        BigDecimal nivelMarePico;
        LocalDateTime horarioPico;
        if (picoPrevisao.isPresent()) {
            nivelMarePico = picoPrevisao.get().getNivelMare().setScale(2, RoundingMode.HALF_UP);
            horarioPico = picoPrevisao.get().getDataHora();
        } else if (telemetria.getPreamarNivelPrevisto() != null && telemetria.getPreamarHorario() != null) {
            nivelMarePico = telemetria.getPreamarNivelPrevisto().setScale(2, RoundingMode.HALF_UP);
            horarioPico = telemetria.getPreamarHorario();
        } else {
            nivelMarePico = nivelMareAtual;
            horarioPico = telemetria.getPreamarHorario() != null
                    ? telemetria.getPreamarHorario()
                    : instanteAlvo.atZone(ZoneId.of("America/Belem")).toLocalDateTime();
        }

        String cotaFormatada = String.format(Locale.US, "%.2f m", nivelMarePico.doubleValue());

        Double cotaTransbordamento = (bairro != null && bairro.getCotaTransbordamentoMetros() != null)
                ? bairro.getCotaTransbordamentoMetros()
                : 3.00;

        String statusCota;
        if (nivelMarePico.doubleValue() >= cotaTransbordamento) {
            statusCota = "TRANSBORDAMENTO";
        } else if (nivelMarePico.doubleValue() >= (cotaTransbordamento - 0.30)) {
            statusCota = "ALERTA";
        } else {
            statusCota = "NORMAL";
        }

        NivelRisco nivelRisco = calcularRiscoTopografico(bairro, nivelMarePico.doubleValue(), probArredondada);
        String descricao = gerarDescricaoCenario(nivelRisco, probArredondada, nivelMarePico, bairro);
        MerchantVerdict verdict = buildMerchantVerdict(
                nivelRisco,
                statusCota,
                probArredondada,
                probPercentual,
                nivelMarePico.doubleValue(),
                cotaTransbordamento,
                horarioPico,
                responseMl.getExpectedPrecipitationMm(),
                previsoes12h,
                calibration.openMeteoResponse(),
                bairro
        );

        return RiscoResponse.builder()
                .bairro(estacao)
                .nivelRisco(nivelRisco)
                .probabilidadeChuva(probArredondada)
                .probabilidadePercentual(probPercentual)
                .probabilidadeFormatada(probFormatada)
                .nivelMarePrevistoMetros(nivelMarePico)
                .nivelMareAtualMetros(nivelMareAtual)
                .cotaFormatada(cotaFormatada)
                .cotaTransbordamentoMetros(cotaTransbordamento)
                .statusCota(statusCota)
                .descricaoCenario(descricao)
                .horarioPicoPrevisto(horarioPico)
                .alertaEmitido(nivelRisco == NivelRisco.ALTO || nivelRisco == NivelRisco.CRITICO)
                .vaiAlagar(verdict.vaiAlagar())
                .riscoAlagamentoSimples(verdict.riscoAlagamentoSimples())
                .vaiChover(verdict.vaiChover())
                .intensidadeChuvaPrevista(verdict.intensidadeChuvaPrevista())
                .horarioChuvaInicio(verdict.horarioChuvaInicio())
                .horarioChuvaFim(verdict.horarioChuvaFim())
                .chuvaComMareAlta(verdict.chuvaComMareAlta())
                .janelaCriticaInicio(verdict.janelaCriticaInicio())
                .janelaCriticaFim(verdict.janelaCriticaFim())
                .mensagemCurta(verdict.mensagemCurta())
                .build();
    }

    private CalibrationResult calibrarProbabilidade(Double probMl, double lat, double lon) {
        OpenMeteoClient.OpenMeteoResponse response = null;
        try {
            response = openMeteoClient.buscarDadosAtuaisEPrevisao(lat, lon);
            if (response != null && response.hourly() != null && response.hourly().probabilidadePrecipitacao() != null && !response.hourly().probabilidadePrecipitacao().isEmpty()) {
                BigDecimal probEnsemble = response.hourly().probabilidadePrecipitacao().get(0);
                if (probEnsemble != null) {
                    double pEnsemble = probEnsemble.doubleValue() / 100.0;
                    return new CalibrationResult((pEnsemble * 0.70) + (probMl * 0.30), response);
                }
            }
        } catch (Exception ex) {
            log.debug("Ensemble Open-Meteo nao consultado ou indisponivel: {}", ex.getMessage());
        }
        return new CalibrationResult(probMl, response);
    }

    private NivelRisco calcularRiscoTopografico(Bairro bairro, double mare, double prob) {
        double cota = (bairro != null && bairro.getCotaTransbordamentoMetros() != null)
                ? bairro.getCotaTransbordamentoMetros()
                : 3.00;

        boolean isBairroAlto = cota >= 6.00;

        if (!isBairroAlto) {
            boolean transbordando = mare >= cota;
            boolean transbordamentoSevero = mare >= (cota + 0.40) || mare >= 3.50 || (transbordando && prob >= 0.70);
            boolean quaseTransbordando = mare >= (cota - 0.25);
            boolean cotaAlerta = mare >= (cota - 0.55);

            if (transbordamentoSevero) {
                return NivelRisco.CRITICO;
            }
            if (transbordando || (quaseTransbordando && prob >= 0.30) || (mare >= 3.00 && prob >= 0.50) || prob >= 0.85) {
                return NivelRisco.ALTO;
            }
            if (cotaAlerta || mare >= 2.50 || prob >= 0.40) {
                return NivelRisco.MEDIO;
            }
            return NivelRisco.BAIXO;
        }

        if (prob >= 0.85) {
            return NivelRisco.ALTO;
        }
        if (prob >= 0.40) {
            return NivelRisco.MEDIO;
        }
        return NivelRisco.BAIXO;
    }

    private String gerarDescricaoCenario(NivelRisco risco, Double probChuva, BigDecimal nivelMare, Bairro bairro) {
        String nomeLocal = bairro != null ? bairro.getDescricao() : "Belém";
        double mare = nivelMare != null ? nivelMare.doubleValue() : 0.0;
        double prob = probChuva != null ? probChuva * 100 : 0.0;
        double cota = (bairro != null && bairro.getCotaTransbordamentoMetros() != null) ? bairro.getCotaTransbordamentoMetros() : 3.00;

        if (risco == NivelRisco.CRITICO || risco == NivelRisco.ALTO) {
            if (mare >= cota && cota < 6.0) {
                return String.format(Locale.US, "ALERTA CRÍTICO: Nível da maré (%.2fm) atingiu a cota de transbordamento de %s (%.2fm). Risco iminente de alagamento.",
                        mare, nomeLocal, cota);
            }
            return String.format(Locale.US, "ALERTA CRÍTICO: Combinação de maré elevada (%.2fm) com alta probabilidade de chuva (%.0f%%).",
                    mare, prob);
        } else if (risco == NivelRisco.MEDIO) {
            return String.format(Locale.US, "ATENÇÃO: Nível da maré em %.2fm e probabilidade de chuva em %.0f%%.",
                    mare, prob);
        }
        return "Condições normais. Sem risco significativo de alagamento.";
    }

    private MerchantVerdict buildMerchantVerdict(
            NivelRisco nivelRisco,
            String statusCota,
            double probArredondada,
            int probPercentual,
            double marePico,
            double cotaTransbordamento,
            LocalDateTime horarioPico,
            BigDecimal mlRainMm,
            List<LeituraTelemetria> previsoes12h,
            OpenMeteoClient.OpenMeteoResponse openMeteoResponse,
            Bairro bairro) {

        boolean vaiAlagar = nivelRisco == NivelRisco.CRITICO
                || ("TRANSBORDAMENTO".equals(statusCota) && cotaTransbordamento < 6.0);
        String riscoSimples = vaiAlagar
                ? "PROVAVEL"
                : (nivelRisco == NivelRisco.ALTO || nivelRisco == NivelRisco.MEDIO ? "ATENCAO" : "IMPROVAVEL");

        List<RainSlot> rainSlots = extractRainSlots(previsoes12h, openMeteoResponse);
        double maxRainMm = mlRainMm != null ? mlRainMm.doubleValue() : 0.0;
        for (RainSlot slot : rainSlots) {
            maxRainMm = Math.max(maxRainMm, slot.rainMm());
        }

        boolean vaiChover = probPercentual >= 50 || !rainSlots.isEmpty() || maxRainMm >= 0.5;
        String intensidade = resolveRainIntensity(vaiChover, maxRainMm, probArredondada);

        DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm");
        String chuvaInicio = !rainSlots.isEmpty() ? rainSlots.get(0).time().format(hhmm) : null;
        String chuvaFim = !rainSlots.isEmpty() ? rainSlots.get(rainSlots.size() - 1).time().format(hhmm) : null;

        boolean mareSignificativa = cotaTransbordamento < 6.0 && marePico >= Math.min(cotaTransbordamento - 0.30, 3.00);
        List<LocalDateTime> overlapHours = new ArrayList<>();
        if (mareSignificativa && horarioPico != null && vaiChover) {
            LocalDateTime minMare = horarioPico.minusHours(2);
            LocalDateTime maxMare = horarioPico.plusHours(2);
            for (RainSlot slot : rainSlots) {
                if (!slot.time().isBefore(minMare) && !slot.time().isAfter(maxMare)) {
                    overlapHours.add(slot.time());
                }
            }
        }

        boolean chuvaComMareAlta = !overlapHours.isEmpty() || (mareSignificativa && probPercentual >= 65 && rainSlots.isEmpty());
        String janelaInicio = !overlapHours.isEmpty()
                ? overlapHours.get(0).format(hhmm)
                : (chuvaComMareAlta && horarioPico != null ? horarioPico.minusHours(1).format(hhmm) : null);
        String janelaFim = !overlapHours.isEmpty()
                ? overlapHours.get(overlapHours.size() - 1).format(hhmm)
                : (chuvaComMareAlta && horarioPico != null ? horarioPico.plusHours(1).format(hhmm) : null);

        String nomeLocal = bairro != null ? bairro.getDescricao() : "seu ponto";
        String picoHoraStr = horarioPico != null ? horarioPico.format(hhmm) : "--:--";
        String mensagemCurta = composeShortMerchantMessage(
                nomeLocal, vaiAlagar, riscoSimples, vaiChover, probPercentual,
                chuvaComMareAlta, janelaInicio, janelaFim, chuvaInicio, chuvaFim, picoHoraStr
        );

        return new MerchantVerdict(
                vaiAlagar, riscoSimples, vaiChover, intensidade,
                chuvaInicio, chuvaFim, chuvaComMareAlta, janelaInicio, janelaFim, mensagemCurta
        );
    }

    private List<RainSlot> extractRainSlots(List<LeituraTelemetria> previsoes12h, OpenMeteoClient.OpenMeteoResponse openMeteoResponse) {
        List<RainSlot> slots = new ArrayList<>();
        if (previsoes12h != null && !previsoes12h.isEmpty()) {
            for (LeituraTelemetria l : previsoes12h) {
                if (l.getDataHora() == null) continue;
                double mm = l.getPrecipitacao() != null ? l.getPrecipitacao().doubleValue() : 0.0;
                int prob = l.getProbabilidadeChuva() != null ? l.getProbabilidadeChuva() : 0;
                if (mm >= 0.5 || prob >= 50) {
                    slots.add(new RainSlot(l.getDataHora(), mm));
                }
            }
            return slots;
        }
        if (openMeteoResponse != null && openMeteoResponse.hourly() != null && openMeteoResponse.hourly().time() != null) {
            var hourly = openMeteoResponse.hourly();
            LocalDateTime agora = LocalDateTime.now(ZoneId.of("America/Belem"));
            LocalDateTime limite = agora.plusHours(12);
            for (int i = 0; i < hourly.time().size(); i++) {
                try {
                    LocalDateTime dt = LocalDateTime.parse(hourly.time().get(i));
                    if (dt.isBefore(agora.minusMinutes(30)) || dt.isAfter(limite)) continue;
                    double mm = (hourly.precipitacao() != null && i < hourly.precipitacao().size() && hourly.precipitacao().get(i) != null)
                            ? hourly.precipitacao().get(i).doubleValue() : 0.0;
                    double prob = (hourly.probabilidadePrecipitacao() != null && i < hourly.probabilidadePrecipitacao().size() && hourly.probabilidadePrecipitacao().get(i) != null)
                            ? hourly.probabilidadePrecipitacao().get(i).doubleValue() : 0.0;
                    if (mm >= 0.5 || prob >= 50) {
                        slots.add(new RainSlot(dt, mm));
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return slots;
    }

    private String resolveRainIntensity(boolean vaiChover, double maxRainMm, double probArredondada) {
        if (!vaiChover && maxRainMm < 0.2) return "SEM_CHUVA";
        if (maxRainMm >= 10.0 || (vaiChover && probArredondada >= 0.80 && maxRainMm >= 4.0)) return "FORTE";
        if (maxRainMm >= 2.5 || probArredondada >= 0.65) return "MODERADA";
        return "FRACA";
    }

    private String composeShortMerchantMessage(
            String nomeLocal,
            boolean vaiAlagar,
            String riscoSimples,
            boolean vaiChover,
            int probPercentual,
            boolean chuvaComMareAlta,
            String janelaInicio,
            String janelaFim,
            String chuvaInicio,
            String chuvaFim,
            String picoHoraStr) {

        if (vaiAlagar && chuvaComMareAlta && janelaInicio != null && janelaFim != null) {
            return String.format("Risco de alagamento em %s entre %s e %s (chuva + maré alta).", nomeLocal, janelaInicio, janelaFim);
        }
        if (vaiAlagar) {
            return String.format("Risco de água na rua em %s com pico da maré por volta das %s.", nomeLocal, picoHoraStr);
        }
        if ("ATENCAO".equals(riscoSimples) && vaiChover) {
            if (chuvaComMareAlta && janelaInicio != null && janelaFim != null) {
                return String.format("Atenção em %s: chuva (%d%%) coincide com maré alta entre %s e %s.", nomeLocal, probPercentual, janelaInicio, janelaFim);
            }
            if (chuvaInicio != null && chuvaFim != null) {
                return String.format("Chuva prevista (%d%%) entre %s e %s em %s; acompanhe o nível.", probPercentual, chuvaInicio, chuvaFim, nomeLocal);
            }
            return String.format("Atenção em %s: chance de chuva em %d%% com maré elevada.", nomeLocal, probPercentual);
        }
        if (vaiChover) {
            return String.format("Chance de chuva (%d%%) em %s, mas sem risco relevante de alagamento.", probPercentual, nomeLocal);
        }
        return String.format("Sem previsão de chuva forte ou alagamento em %s nas próximas horas.", nomeLocal);
    }

    private record CalibrationResult(double probabilidade, OpenMeteoClient.OpenMeteoResponse openMeteoResponse) {}
    private record RainSlot(LocalDateTime time, double rainMm) {}
    private record MerchantVerdict(
            boolean vaiAlagar,
            String riscoAlagamentoSimples,
            boolean vaiChover,
            String intensidadeChuvaPrevista,
            String horarioChuvaInicio,
            String horarioChuvaFim,
            boolean chuvaComMareAlta,
            String janelaCriticaInicio,
            String janelaCriticaFim,
            String mensagemCurta
    ) {}

    private PrevisaoMlResponse criarPrevisaoFallback(TelemetriaDashboardResponse telemetria) {
        BigDecimal chuva = (telemetria != null && telemetria.getChuvaAtual() != null) ? telemetria.getChuvaAtual() : BigDecimal.ZERO;
        double chuvaVal = chuva.doubleValue();

        double prob;
        if (chuvaVal >= 20.0) {
            prob = 0.85;
        } else if (chuvaVal > 0.0) {
            prob = 0.50;
        } else {
            prob = 0.15;
        }

        Instant agora = Instant.now();
        Instant alvo = agora.plus(1, ChronoUnit.HOURS);

        return PrevisaoMlResponse.builder()
                .modelVersion("fallback-heuristic-v1")
                .issuedAt(agora)
                .targetTime(alvo)
                .horizonHours(1)
                .rainProbability(prob)
                .rainExpected(prob >= 0.50)
                .expectedPrecipitationMm(chuva)
                .qualityStatus(StatusQualidadeMl.DEGRADADO)
                .build();
    }
}
