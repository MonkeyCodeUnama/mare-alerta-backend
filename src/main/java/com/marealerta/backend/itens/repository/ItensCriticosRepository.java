package com.marealerta.backend.itens.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marealerta.backend.itens.model.ItemCritico;

public interface ItensCriticosRepository extends JpaRepository<ItemCritico, Long> {
    
    List<ItemCritico> findByEstabelecimentoId(Long estabelecimentoId);
}
