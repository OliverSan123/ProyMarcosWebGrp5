package com.gruponorte.mesaayudati.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Representa un ticket y sus fechas de creación y objetivo. */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String prioridad;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(nullable = false, length = 100)
    private String area;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaObjetivo;

    /** Constructor requerido por JPA. */
    protected Ticket() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaObjetivo() { return fechaObjetivo; }
    public void setFechaObjetivo(LocalDateTime fechaObjetivo) { this.fechaObjetivo = fechaObjetivo; }
}