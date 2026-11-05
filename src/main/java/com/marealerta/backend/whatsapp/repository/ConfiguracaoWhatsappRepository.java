package com.marealerta.backend.whatsapp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.marealerta.backend.whatsapp.model.ConfiguracaoWhatsapp;

public interface ConfiguracaoWhatsappRepository extends JpaRepository<ConfiguracaoWhatsapp, Long> {
    Optional<ConfiguracaoWhatsapp> findFirstByAtivoTrueOrderByIdAsc();
}
