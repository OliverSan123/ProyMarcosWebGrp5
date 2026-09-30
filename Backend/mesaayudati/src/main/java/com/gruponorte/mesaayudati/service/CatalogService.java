package com.gruponorte.mesaayudati.service;

import com.gruponorte.mesaayudati.dto.CatalogRequest;
import com.gruponorte.mesaayudati.dto.CatalogResponse;
import com.gruponorte.mesaayudati.dto.PriorityRuleRequest;
import com.gruponorte.mesaayudati.dto.PriorityRuleResponse;
import com.gruponorte.mesaayudati.entity.Area;
import com.gruponorte.mesaayudati.entity.Category;
import com.gruponorte.mesaayudati.entity.PriorityRule;
import com.gruponorte.mesaayudati.entity.Priority;
import com.gruponorte.mesaayudati.repository.AreaRepository;
import com.gruponorte.mesaayudati.repository.CategoryRepository;
import com.gruponorte.mesaayudati.repository.PriorityRuleRepository;
import com.gruponorte.mesaayudati.repository.PriorityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Reglas CRUD compartidas de los tres catálogos de administración. */
@Service
@Transactional
public class CatalogService {

    private final AreaRepository areaRepository;
    private final CategoryRepository categoryRepository;
    private final PriorityRepository priorityRepository;
    private final PriorityRuleRepository priorityRuleRepository;

    public CatalogService(AreaRepository areaRepository, CategoryRepository categoryRepository,
                          PriorityRepository priorityRepository, PriorityRuleRepository priorityRuleRepository) {
        this.areaRepository = areaRepository;
        this.categoryRepository = categoryRepository;
        this.priorityRepository = priorityRepository;
        this.priorityRuleRepository = priorityRuleRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogResponse> areas() {
        return areaRepository.findAll().stream().map(area -> response(area.getId(), area.getName(), null)).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogResponse> categorias() {
        return categoryRepository.findAll().stream()
                .map(category -> response(category.getId(), category.getName(), null)).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogResponse> prioridades() {
        return priorityRepository.findAll().stream()
                .map(priority -> response(priority.getId(), priority.getName(), priority.getSlaHours())).toList();
    }

    @Transactional(readOnly = true)
    public List<PriorityRuleResponse> matrizPrioridad() {
        return priorityRuleRepository.findAll().stream().map(this::toRuleResponse).toList();
    }

    public PriorityRuleResponse crearRegla(PriorityRuleRequest request) {
        if (priorityRuleRepository.existsByImpactAndUrgency(request.impact(), request.urgency())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una regla para esa combinación de impacto y urgencia");
        }
        PriorityRule rule = new PriorityRule();
        rule.setImpact(request.impact());
        rule.setUrgency(request.urgency());
        rule.setPriority(getPriority(request.priorityId()));
        return toRuleResponse(priorityRuleRepository.save(rule));
    }

    public PriorityRuleResponse actualizarRegla(Long id, PriorityRuleRequest request) {
        PriorityRule rule = priorityRuleRepository.findById(id).orElseThrow(() -> notFound("Regla"));
        if (priorityRuleRepository.existsByImpactAndUrgencyAndIdNot(request.impact(), request.urgency(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una regla para esa combinación de impacto y urgencia");
        }
        rule.setImpact(request.impact());
        rule.setUrgency(request.urgency());
        rule.setPriority(getPriority(request.priorityId()));
        return toRuleResponse(priorityRuleRepository.save(rule));
    }

    public void eliminarRegla(Long id) {
        priorityRuleRepository.delete(priorityRuleRepository.findById(id)
                .orElseThrow(() -> notFound("Regla")));
    }

    public CatalogResponse crearArea(CatalogRequest request) {
        if (areaRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw duplicateName();
        }
        Area area = new Area();
        area.setName(request.name().trim());
        return response(areaRepository.save(area).getId(), area.getName(), null);
    }

    public CatalogResponse actualizarArea(Long id, CatalogRequest request) {
        Area area = areaRepository.findById(id).orElseThrow(() -> notFound("Área"));
        areaRepository.findByNameIgnoreCase(request.name().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw duplicateName();
        });
        area.setName(request.name().trim());
        areaRepository.save(area);
        return response(area.getId(), area.getName(), null);
    }

    public void eliminarArea(Long id) {
        areaRepository.delete(areaRepository.findById(id).orElseThrow(() -> notFound("Área")));
    }

    public CatalogResponse crearCategoria(CatalogRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw duplicateName();
        }
        Category category = new Category();
        category.setName(request.name().trim());
        categoryRepository.save(category);
        return response(category.getId(), category.getName(), null);
    }

    public CatalogResponse actualizarCategoria(Long id, CatalogRequest request) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> notFound("Categoría"));
        categoryRepository.findByNameIgnoreCase(request.name().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw duplicateName();
        });
        category.setName(request.name().trim());
        categoryRepository.save(category);
        return response(category.getId(), category.getName(), null);
    }

    public void eliminarCategoria(Long id) {
        categoryRepository.delete(categoryRepository.findById(id).orElseThrow(() -> notFound("Categoría")));
    }

    public CatalogResponse crearPrioridad(CatalogRequest request) {
        if (request.slaHours() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La prioridad requiere slaHours");
        }
        if (priorityRepository.existsByNameIgnoreCase(request.name().trim())) throw duplicateName();
        Priority priority = new Priority();
        priority.setName(request.name().trim());
        priority.setSlaHours(request.slaHours());
        priorityRepository.save(priority);
        return response(priority.getId(), priority.getName(), priority.getSlaHours());
    }

    public CatalogResponse actualizarPrioridad(Long id, CatalogRequest request) {
        if (request.slaHours() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La prioridad requiere slaHours");
        }
        Priority priority = priorityRepository.findById(id).orElseThrow(() -> notFound("Prioridad"));
        priorityRepository.findByNameIgnoreCase(request.name().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw duplicateName();
        });
        priority.setName(request.name().trim());
        priority.setSlaHours(request.slaHours());
        priorityRepository.save(priority);
        return response(priority.getId(), priority.getName(), priority.getSlaHours());
    }

    public void eliminarPrioridad(Long id) {
        priorityRepository.delete(priorityRepository.findById(id).orElseThrow(() -> notFound("Prioridad")));
    }

    private CatalogResponse response(Long id, String name, Integer slaHours) {
        return new CatalogResponse(id, name, slaHours);
    }

    private PriorityRuleResponse toRuleResponse(PriorityRule rule) {
        return new PriorityRuleResponse(rule.getId(), rule.getImpact().name(), rule.getUrgency().name(),
                rule.getPriority().getId(), rule.getPriority().getName());
    }

    private Priority getPriority(Long id) {
        return priorityRepository.findById(id).orElseThrow(() -> notFound("Prioridad"));
    }

    private ResponseStatusException duplicateName() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un elemento con ese nombre");
    }

    private ResponseStatusException notFound(String type) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " no encontrada");
    }
}