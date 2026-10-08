package com.marealerta.backend.telemetria.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marealerta.backend.telemetria.model.LeituraTelemetria;
import com.marealerta.backend.telemetria.model.TipoRegistroTelemetria;

public interface LeituraTelemetriaRepository extends JpaRepository<LeituraTelemetria, Long> {

    Optional<LeituraTelemetria> findTop1ByOrderByDataHoraDesc();

    List<LeituraTelemetria> findTop10ByEstacaoCodigoOrderByDataHoraDesc(String estacaoCodigo);

    Optional<LeituraTelemetria> findTop1ByEstacaoCodigoAndTipoRegistroOrderByDataHoraDesc(
            String estacaoCodigo, TipoRegistroTelemetria tipoRegistro);

    List<LeituraTelemetria> findByEstacaoCodigoAndDataHoraBetweenOrderByDataHoraAsc(
            String estacaoCodigo, LocalDateTime inicio, LocalDateTime fim);

    List<LeituraTelemetria> findByTipoRegistroAndDataHoraBetweenOrderByDataHoraAsc(
            TipoRegistroTelemetria tipoRegistro, LocalDateTime inicio, LocalDateTime fim);
}
