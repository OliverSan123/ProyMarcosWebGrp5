package com.gruponorte.mesaayudati.repository;

import com.gruponorte.mesaayudati.entity.ImpactLevel;
import com.gruponorte.mesaayudati.entity.PriorityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Consulta reglas de la matriz por combinación única de impacto y urgencia. */
public interface PriorityRuleRepository extends JpaRepository<PriorityRule, Long> {
    Optional<PriorityRule> findByImpactAndUrgency(ImpactLevel impact, ImpactLevel urgency);
    boolean existsByImpactAndUrgency(ImpactLevel impact, ImpactLevel urgency);
    boolean existsByImpactAndUrgencyAndIdNot(ImpactLevel impact, ImpactLevel urgency, Long id);
}