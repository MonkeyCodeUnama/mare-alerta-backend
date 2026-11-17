package com.marealerta.backend.risco.dto;

import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.checklist.dto.ChecklistResumoDTO;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PainelResumoDTO {

    private EstabelecimentoResponse estabelecimento;
    private RiscoResponse risco;
    private TelemetriaDashboardResponse telemetriaDashboard;
    private ChecklistResumoDTO checklistResumo;
    private AlertaResponse ultimoAlerta;
}
