package com.gruponorte.mesaayudati.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Ticket y relaciones necesarias para control de acceso, filtros y cálculo de SLA. */
@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_state_created", columnList = "state,created_at"),
        @Index(name = "idx_ticket_area", columnList = "area_id"),
        @Index(name = "idx_ticket_category", columnList = "category_id"),
        @Index(name = "idx_ticket_priority", columnList = "priority_id"),
        @Index(name = "idx_ticket_technician", columnList = "technician_id"),
        @Index(name = "idx_ticket_creator", columnList = "creator_id")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "priority_id", nullable = false)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private AppUser creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private AppUser assignedTechnician;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImpactLevel impact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImpactLevel urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus state;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "target_at")
    private LocalDateTime targetAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public Ticket() {
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public AppUser getCreator() { return creator; }
    public void setCreator(AppUser creator) { this.creator = creator; }
    public AppUser getAssignedTechnician() { return assignedTechnician; }
    public void setAssignedTechnician(AppUser assignedTechnician) { this.assignedTechnician = assignedTechnician; }
    public ImpactLevel getImpact() { return impact; }
    public void setImpact(ImpactLevel impact) { this.impact = impact; }
    public ImpactLevel getUrgency() { return urgency; }
    public void setUrgency(ImpactLevel urgency) { this.urgency = urgency; }
    public TicketStatus getState() { return state; }
    public void setState(TicketStatus state) { this.state = state; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getTargetAt() { return targetAt; }
    public void setTargetAt(LocalDateTime targetAt) { this.targetAt = targetAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}