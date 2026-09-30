package com.gruponorte.mesaayudati.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Registro append-only de estados, asignaciones, comentarios y reaperturas. */
@Entity
@Table(name = "historial_ticket")
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @Column(nullable = false, length = 30, updatable = false)
    private String eventType;

    @Column(length = 120, updatable = false)
    private String oldValue;

    @Column(length = 120, updatable = false)
    private String newValue;

    @Column(length = 3000, updatable = false)
    private String details;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false, updatable = false)
    private AppUser author;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected TicketHistory() {
    }

    public TicketHistory(Ticket ticket, String eventType, String oldValue, String newValue,
                         String details, AppUser author, LocalDateTime createdAt) {
        this.ticket = ticket;
        this.eventType = eventType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.details = details;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public String getEventType() { return eventType; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getDetails() { return details; }
    public AppUser getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}