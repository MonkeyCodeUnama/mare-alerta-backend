package com.marealerta.backend.ocorrencias.service;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaFotoResponse;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaRequest;
import com.marealerta.backend.ocorrencias.dto.OcorrenciaResponse;
import com.marealerta.backend.ocorrencias.model.Ocorrencia;
import com.marealerta.backend.ocorrencias.model.OcorrenciaFoto;
import com.marealerta.backend.ocorrencias.repository.OcorrenciaFotoRepository;
import com.marealerta.backend.ocorrencias.repository.OcorrenciaRepository;
import com.marealerta.backend.usuario.model.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OcorrenciaService {

    @Value("${app.uploads.ocorrencias-dir:uploads/ocorrencias}")
    private String ocorrenciaDir;
    private final OcorrenciaRepository oRepository;
    private final OcorrenciaFotoRepository ofRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    @Transactional
    public OcorrenciaResponse registrarO(Usuario usuario, OcorrenciaRequest ocorrenciaRequest) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario nao autenticado");
        }
        if (usuario.getEstabelecimento() == null || usuario.getEstabelecimento().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuario nao possui estabelecimento vinculado");
        }

        Long estabId = usuario.getEstabelecimento().getId();

        Estabelecimento estab = estabelecimentoRepository.findById(estabId)
                .orElseThrow(() -> new EntityNotFoundException("estabelecimento nao encontrado"));

        long quant = oRepository.countByEstabelecimentoId(estab.getId());
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

        return OcorrenciaResponse.fromEntity(ocorrencia);
    }

    public OcorrenciaFotoResponse adicionarFoto(Usuario usuario, Long ocorrenciaId, MultipartFile arquivo) throws IOException {
        if (usuario == null || usuario.getEstabelecimento() == null || usuario.getEstabelecimento().getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario nao autenticado ou sem estabelecimento");
        }
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "arquivo de foto invalido ou vazio");
        }

        Ocorrencia ocorrencia = oRepository.findById(ocorrenciaId)
                .orElseThrow(() -> new EntityNotFoundException("ocorrencia nao encontrada"));

        if (!Objects.equals(ocorrencia.getEstabelecimento().getId(), usuario.getEstabelecimento().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "usuario nao autorizado a anexar foto nesta ocorrencia");
        }

        String nomeArquivo = arquivo.getOriginalFilename();
        String nomeFoto = UUID.randomUUID().toString() + "-" + Paths.get(nomeArquivo != null ? nomeArquivo : "foto.jpg").getFileName();

        Files.createDirectories(Paths.get(ocorrenciaDir));

        Path destino = Paths.get(ocorrenciaDir, nomeFoto);

         try {
             arquivo.transferTo(destino.toFile());
         }catch (IOException e){
             throw new RuntimeException("erro ao salvar arquivo", e);
         }

         String urlFoto = "uploads/ocorrencias/" + nomeFoto;

         OcorrenciaFoto foto = OcorrenciaFoto.builder()
                 .nomeArquivo(nomeArquivo)
                 .urlFoto(urlFoto)
                 .tamanhoBytes(arquivo.getSize())
                 .contentType(arquivo.getContentType())
                 .ocorrencia(ocorrencia)
                 .build();

         foto = ofRepository.save(foto);

         OcorrenciaFotoResponse response = OcorrenciaFotoResponse.fromEntity(foto);

         return response;
     }

    public ResponseEntity<Resource> baixarFoto(Usuario usuario, Long ocorrenciaId, Long fotoId) throws IOException {
        if (usuario == null || usuario.getEstabelecimento() == null || usuario.getEstabelecimento().getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario nao autenticado ou sem estabelecimento");
        }

        Ocorrencia ocorrencia = oRepository.findById(ocorrenciaId)
                .orElseThrow(() -> new EntityNotFoundException("ocorrencia nao encontrada"));
        if (!Objects.equals(ocorrencia.getEstabelecimento().getId(), usuario.getEstabelecimento().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "usuario nao autorizado a acessar esta ocorrencia");
        }

        OcorrenciaFoto foto = ofRepository.findById(fotoId)
                .filter(item -> Objects.equals(item.getOcorrencia().getId(), ocorrenciaId))
                .orElseThrow(() -> new EntityNotFoundException("foto nao encontrada"));
        Resource resource = new FileSystemResource(Paths.get(ocorrenciaDir, foto.getUrlFoto().substring(foto.getUrlFoto().lastIndexOf('/') + 1)));
        if (!resource.exists() || !resource.isReadable()) {
            throw new EntityNotFoundException("arquivo da foto nao encontrado");
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (foto.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(foto.getContentType());
            } catch (IllegalArgumentException ignored) {
                // Mantém o tipo binário quando o MIME salvo for inválido.
            }
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + foto.getNomeArquivo() + "\"")
                .body(resource);
    }
}
