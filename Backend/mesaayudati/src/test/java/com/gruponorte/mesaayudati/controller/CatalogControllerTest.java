package com.gruponorte.mesaayudati.controller;

import com.gruponorte.mesaayudati.dto.*;
import com.gruponorte.mesaayudati.service.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Equivalente a ProductoControllerTest, adaptado al catálogo de la mesa de ayuda. */
class CatalogControllerTest {
    CatalogService service = mock(CatalogService.class);
    MockMvc mvc;
    @BeforeEach void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new CatalogController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }
    @Test void crearDevuelve201() throws Exception {
        when(service.crearCategoria(any())).thenReturn(new CatalogResponse(3L,"Redes",null));
        mvc.perform(post("/api/catalogos/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Redes\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(3));
    }
    @Test void nombreVacioSeRechazaAntesDelServicio() throws Exception {
        mvc.perform(post("/api/catalogos/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.name").value("El nombre es obligatorio"));
        verifyNoInteractions(service);
    }
    @Test void slaNegativoSeRechazaAntesDelServicio() throws Exception {
        mvc.perform(post("/api/catalogos/prioridades").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Alta\",\"slaHours\":-1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.slaHours").exists());
        verifyNoInteractions(service);
    }
    @Test void consultarIndividualDevuelve200() throws Exception {
        when(service.obtenerCategoria(3L)).thenReturn(new CatalogResponse(3L,"Redes",null));
        mvc.perform(get("/api/catalogos/categorias/3")).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Redes"));
    }
    @Test void consultarInexistenteDevuelve404() throws Exception {
        when(service.obtenerCategoria(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoría no encontrada"));
        mvc.perform(get("/api/catalogos/categorias/99")).andExpect(status().isNotFound());
    }
    @Test void actualizarDevuelve200() throws Exception {
        when(service.actualizarCategoria(eq(3L),any())).thenReturn(new CatalogResponse(3L,"Software",null));
        mvc.perform(put("/api/catalogos/categorias/3").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Software\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Software"));
    }
    @Test void eliminarDevuelve204SinCuerpo() throws Exception {
        mvc.perform(delete("/api/catalogos/categorias/3")).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(service).eliminarCategoria(3L);
    }
    @Test void idNoNumericoDevuelve400() throws Exception {
        mvc.perform(get("/api/catalogos/categorias/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parámetro inválido: id"));
        verifyNoInteractions(service);
    }
    @Test void jsonMalformadoDevuelve400() throws Exception {
        mvc.perform(post("/api/catalogos/categorias").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
