package com.gruponorte.mesaayudati.service;

import com.gruponorte.mesaayudati.dto.*;
import com.gruponorte.mesaayudati.entity.*;
import com.gruponorte.mesaayudati.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock AreaRepository areas;
    @Mock CategoryRepository categories;
    @Mock PriorityRepository priorities;
    @Mock PriorityRuleRepository rules;
    CatalogService service;
    @BeforeEach void setup() { service = new CatalogService(areas,categories,priorities,rules); }
    @Test void crearCategoriaNormalizaNombre() {
        doAnswer(inv -> { Category category = inv.getArgument(0); ReflectionTestUtils.setField(category,"id",3L); return category; })
                .when(categories).save(any(Category.class));
        var result = service.crearCategoria(new CatalogRequest("  Redes  ",null));
        assertEquals(3L,result.id()); assertEquals("Redes",result.name());
        verify(categories).existsByNameIgnoreCase("Redes");
    }
    @Test void duplicadoNoSeGuarda() {
        when(categories.existsByNameIgnoreCase("Redes")).thenReturn(true);
        var ex = assertThrows(ResponseStatusException.class,()->service.crearCategoria(new CatalogRequest("Redes",null)));
        assertEquals(409,ex.getStatusCode().value()); verify(categories,never()).save(any());
    }
    @Test void actualizarConservaId() {
        Category c = new Category(); ReflectionTestUtils.setField(c,"id",7L); c.setName("Redes");
        when(categories.findById(7L)).thenReturn(Optional.of(c));
        var result=service.actualizarCategoria(7L,new CatalogRequest("Software",null));
        assertEquals(7L,result.id()); assertEquals("Software",result.name()); verify(categories).save(c);
    }
    @Test void eliminarBuscaAntesDeBorrar() {
        Category c = new Category(); when(categories.findById(7L)).thenReturn(Optional.of(c));
        service.eliminarCategoria(7L); verify(categories).delete(c);
    }
    @Test void eliminarInexistenteNoBorra() {
        var ex=assertThrows(ResponseStatusException.class,()->service.eliminarCategoria(99L));
        assertEquals(404,ex.getStatusCode().value()); verify(categories,never()).delete(any());
    }
    @Test void reglaRechazaPrioridadInexistente() {
        var ex=assertThrows(ResponseStatusException.class,()->service.crearRegla(new PriorityRuleRequest(ImpactLevel.ALTO,ImpactLevel.ALTO,99L)));
        assertEquals(404,ex.getStatusCode().value()); verify(rules,never()).save(any());
    }
    @Test void prioridadRequiereSlaPositivo() {
        for(Integer hours : new Integer[]{null,0,-1}) {
            var ex=assertThrows(ResponseStatusException.class,()->service.crearPrioridad(new CatalogRequest("Alta",hours)));
            assertEquals(400,ex.getStatusCode().value());
        }
        verify(priorities,never()).save(any());
    }
    @Test void nombrePrioridadRespetaColumnaDe30Caracteres() {
        var ex=assertThrows(ResponseStatusException.class,()->service.crearPrioridad(new CatalogRequest("a".repeat(31),4)));
        assertEquals(400,ex.getStatusCode().value()); verify(priorities,never()).save(any());
    }
}

