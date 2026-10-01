package com.marealerta.backend.ocorrencias.service;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaRequest;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaResponse;
import com.marealerta.backend.ocorrencias.model.Ocorrencia;
import com.marealerta.backend.ocorrencias.repository.OcorrenciaRepository;
import com.marealerta.backend.usuario.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class OcorrenciaService {

    private final OcorrenciaRepository oRepository;

    public OcorrenciaResponse registrarO(Usuario usuario, OcorrenciaRequest ocorrenciaRequest){
        Estabelecimento estab = usuario.getEstabelecimento();

        long quant= oRepository.countByEstabelecimentoId(estab.getId());
        quant += 1;

        String protocolo = "OC-" + estab.getId() + "-" + quant;

        Ocorrencia ocorrencia = Ocorrencia.builder()
                .titulo(ocorrenciaRequest.getTitulo())
                .descricao(ocorrenciaRequest.getDescricao())
                .status(ocorrenciaRequest.getStatus())
                .nivelAlagamento(ocorrenciaRequest.getNivelAlagamento())
                .dataOcorrencia(ocorrenciaRequest.getDataOcorrencia())
                .horaInicio(ocorrenciaRequest.getHoraInicio())
                .horaFim(ocorrenciaRequest.getHoraFim())
                .faixaLaminaAgua(ocorrenciaRequest.getFaixaLaminaAgua())
                .categoriasDano(ocorrenciaRequest.getCategoriasDano())
                .valorEstimadoPrejuizo(ocorrenciaRequest.getValorEstimadoPrejuizo())
                .horasFechado(ocorrenciaRequest.getHorasFechado())
                .tamanhoEquipe(ocorrenciaRequest.getTamanhoEquipe())
                .estabelecimento(estab)
                .protocolo(protocolo)
                .build();

                ocorrencia = oRepository.save(ocorrencia);

                OcorrenciaResponse response = OcorrenciaResponse.fromEntity(ocorrencia);

                return response;
    }
}
