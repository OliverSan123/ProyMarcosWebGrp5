package com.gruponorte.mesaayudati.controller;

import com.gruponorte.mesaayudati.dto.UserRequest;
import com.gruponorte.mesaayudati.dto.UserResponse;
import com.gruponorte.mesaayudati.dto.TechnicianOptionResponse;
import com.gruponorte.mesaayudati.service.UserService;
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

/** Administración de usuarios restringida al rol Administrador TI. */
@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UserResponse> listar() { return userService.listar(); }

    /** Lista mínima de técnicos para los filtros y asignaciones de la bandeja. */
    @GetMapping("/tecnicos")
    @PreAuthorize("isAuthenticated()")
    public List<TechnicianOptionResponse> listarTecnicos() { return userService.listarTecnicos(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UserResponse> crear(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UserResponse actualizar(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        userService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}