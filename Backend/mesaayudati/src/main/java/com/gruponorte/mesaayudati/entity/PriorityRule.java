package com.gruponorte.mesaayudati.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Regla editable que vincula una combinación de impacto/urgencia con una prioridad. */
@Entity
@Table(name = "reglas_prioridad", uniqueConstraints =
        @UniqueConstraint(name = "uk_impacto_urgencia", columnNames = {"impact", "urgency"}))
public class PriorityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImpactLevel impact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImpactLevel urgency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "priority_id", nullable = false)
    private Priority priority;

    public PriorityRule() {
    }

    public Long getId() { return id; }
    public ImpactLevel getImpact() { return impact; }
    public void setImpact(ImpactLevel impact) { this.impact = impact; }
    public ImpactLevel getUrgency() { return urgency; }
    public void setUrgency(ImpactLevel urgency) { this.urgency = urgency; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}