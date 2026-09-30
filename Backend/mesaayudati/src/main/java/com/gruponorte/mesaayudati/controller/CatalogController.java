package com.gruponorte.mesaayudati.controller;

import com.gruponorte.mesaayudati.dto.CatalogRequest;
import com.gruponorte.mesaayudati.dto.CatalogResponse;
import com.gruponorte.mesaayudati.dto.PriorityRuleRequest;
import com.gruponorte.mesaayudati.dto.PriorityRuleResponse;
import com.gruponorte.mesaayudati.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CRUD de áreas, categorías y prioridades disponible solo para administradores. */
@RestController
@RequestMapping("/api/catalogos")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/areas")
    public List<CatalogResponse> areas() { return catalogService.areas(); }
    @PostMapping("/areas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CatalogResponse> crearArea(@Valid @RequestBody CatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.crearArea(request));
    }
    @PutMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CatalogResponse actualizarArea(@PathVariable Long id, @Valid @RequestBody CatalogRequest request) {
        return catalogService.actualizarArea(id, request);
    }
    @DeleteMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarArea(@PathVariable Long id) {
        catalogService.eliminarArea(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categorias")
    public List<CatalogResponse> categorias() { return catalogService.categorias(); }
    @PostMapping("/categorias")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CatalogResponse> crearCategoria(@Valid @RequestBody CatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.crearCategoria(request));
    }
    @PutMapping("/categorias/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CatalogResponse actualizarCategoria(@PathVariable Long id, @Valid @RequestBody CatalogRequest request) {
        return catalogService.actualizarCategoria(id, request);
    }
    @DeleteMapping("/categorias/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        catalogService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/prioridades")
    public List<CatalogResponse> prioridades() { return catalogService.prioridades(); }
    @PostMapping("/prioridades")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CatalogResponse> crearPrioridad(@Valid @RequestBody CatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.crearPrioridad(request));
    }
    @PutMapping("/prioridades/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CatalogResponse actualizarPrioridad(@PathVariable Long id,
                                               @Valid @RequestBody CatalogRequest request) {
        return catalogService.actualizarPrioridad(id, request);
    }
    @DeleteMapping("/prioridades/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarPrioridad(@PathVariable Long id) {
        catalogService.eliminarPrioridad(id);
        return ResponseEntity.noContent().build();
    }

    /** Matriz configurable que asigna prioridad a cada par impacto/urgencia. */
    @GetMapping("/matriz-prioridad")
    public List<PriorityRuleResponse> matrizPrioridad() { return catalogService.matrizPrioridad(); }

    @PostMapping("/matriz-prioridad")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PriorityRuleResponse> crearRegla(@Valid @RequestBody PriorityRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.crearRegla(request));
    }

    @PutMapping("/matriz-prioridad/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public PriorityRuleResponse actualizarRegla(@PathVariable Long id,
                                                @Valid @RequestBody PriorityRuleRequest request) {
        return catalogService.actualizarRegla(id, request);
    }

    @DeleteMapping("/matriz-prioridad/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarRegla(@PathVariable Long id) {
        catalogService.eliminarRegla(id);
        return ResponseEntity.noContent().build();
    }
}