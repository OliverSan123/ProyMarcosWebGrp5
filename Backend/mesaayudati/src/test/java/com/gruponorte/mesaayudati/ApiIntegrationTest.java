package com.gruponorte.mesaayudati;

import com.gruponorte.mesaayudati.dto.*;
import com.gruponorte.mesaayudati.entity.*;
import com.gruponorte.mesaayudati.repository.*;
import com.gruponorte.mesaayudati.service.*;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** API real con filtros JWT, permisos, servicios, transacciones y H2 aislada. */
@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:api-integration;DB_CLOSE_DELAY=-1",
    "app.bootstrap-test-users.password=prueba-local-123"
})
@Transactional
class ApiIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") Filter security;
    @Autowired AuthService auth;
    @Autowired ObjectMapper mapper;
    @Autowired AreaRepository areas;
    @Autowired CategoryRepository categories;
    @Autowired UserRepository users;
    @Autowired UserService userService;
    MockMvc mvc;
    String admin,coordinator,technician,requester;
    Long areaId,categoryId,technicianId;
    @BeforeEach void setup() {
        mvc=MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
        admin=token("admin.prueba"); coordinator=token("coordinador.prueba");
        technician=token("tecnico.prueba"); requester=token("solicitante.prueba");
        Area a=new Area(); a.setName("Área prueba"); areaId=areas.save(a).getId();
        Category c=new Category(); c.setName("Redes prueba"); categoryId=categories.save(c).getId();
        technicianId=users.findByUsernameIgnoreCase("tecnico.prueba").orElseThrow().getId();
    }
    String token(String username) { return "Bearer "+auth.login(new LoginRequest(username,"prueba-local-123")).accessToken(); }
    JsonNode createTicket() throws Exception {
        String body=mapper.writeValueAsString(new TicketCreateRequest("Sin red","No tengo conexión",categoryId,areaId,ImpactLevel.ALTO,ImpactLevel.ALTO));
        return mapper.readTree(mvc.perform(post("/api/tickets").header("Authorization",requester)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.priority").value("CRITICA"))
                .andReturn().getResponse().getContentAsString());
    }
    @Test void autenticacionYPermisos() throws Exception {
        mvc.perform(get("/api/tickets")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin.prueba\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/catalogos/categorias").header("Authorization",requester)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Otra\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios/"+technicianId).header("Authorization",requester))
                .andExpect(status().isForbidden());
    }
    @Test void cicloCompletoDeTicketYReapertura() throws Exception {
        long id=createTicket().get("id").asLong();
        String route="/api/tickets/"+id;
        mvc.perform(put(route+"/cierre").header("Authorization",coordinator)).andExpect(status().isConflict());
        mvc.perform(put(route+"/asignacion").header("Authorization",coordinator)
                .contentType(MediaType.APPLICATION_JSON).content("{\"technicianId\":"+technicianId+"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("ASIGNADO"));
        mvc.perform(put(route+"/estado").header("Authorization",technician)
                .contentType(MediaType.APPLICATION_JSON).content("{\"state\":\"RESUELTO\"}"))
                .andExpect(status().isConflict());
        for(String state:new String[]{"EN_ATENCION","PENDIENTE_USUARIO","EN_ATENCION","RESUELTO"}) {
            mvc.perform(put(route+"/estado").header("Authorization",technician)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"state\":\""+state+"\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.state").value(state));
        }
        mvc.perform(post(route+"/comentarios").header("Authorization",technician)
                .contentType(MediaType.APPLICATION_JSON).content("{\"comment\":\"Conectividad restablecida\"}"))
                .andExpect(status().isCreated());
        mvc.perform(put(route+"/cierre").header("Authorization",coordinator))
                .andExpect(status().isOk()).andExpect(jsonPath("$.closedAt").isNotEmpty());
        mvc.perform(put(route+"/reapertura").header("Authorization",requester)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(route+"/reapertura").header("Authorization",requester)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"La falla regresó\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("REABIERTO"))
                .andExpect(jsonPath("$.closedAt").isEmpty());
        var history=mapper.readTree(mvc.perform(get(route+"/historial").header("Authorization",requester))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(10,history.size());
        assertEquals("CREACION",history.get(0).get("eventType").asText());
        assertEquals("REAPERTURA",history.get(history.size()-1).get("eventType").asText());
        mvc.perform(get("/api/tickets").param("estado","REABIERTO").param("areaId",areaId.toString())
                .header("Authorization",coordinator)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
    }
    @Test void referenciasInvalidasYCamposObligatorios() throws Exception {
        mvc.perform(post("/api/tickets").header("Authorization",requester).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"description\":\"x\",\"categoryId\":0,\"areaId\":-1,\"impact\":\"ALTO\",\"urgency\":\"ALTO\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.title").exists())
                .andExpect(jsonPath("$.details.categoryId").exists()).andExpect(jsonPath("$.details.areaId").exists());
        mvc.perform(post("/api/tickets").header("Authorization",requester).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new TicketCreateRequest("Falla","Detalle",999999L,areaId,ImpactLevel.ALTO,ImpactLevel.ALTO))))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/tickets").param("estado","INVALIDO").header("Authorization",requester))
                .andExpect(status().isBadRequest());
    }
    @Test void crudCategoriaYDuplicados() throws Exception {
        String content=mvc.perform(post("/api/catalogos/categorias").header("Authorization",admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Periféricos\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id=mapper.readTree(content).get("id").asLong();
        mvc.perform(post("/api/catalogos/categorias").header("Authorization",admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" periféricos \"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/catalogos/categorias/"+id).header("Authorization",requester))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Periféricos"));
        mvc.perform(put("/api/catalogos/categorias/"+id).header("Authorization",admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Impresoras\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(delete("/api/catalogos/categorias/"+id).header("Authorization",admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/catalogos/categorias/"+id).header("Authorization",admin)).andExpect(status().isNotFound());
    }
    @Test void consultasIndividuales() throws Exception {
        mvc.perform(get("/api/catalogos/areas/"+areaId).header("Authorization",admin)).andExpect(status().isOk());
        JsonNode priorities=mapper.readTree(mvc.perform(get("/api/catalogos/prioridades").header("Authorization",admin))
                .andReturn().getResponse().getContentAsString());
        mvc.perform(get("/api/catalogos/prioridades/"+priorities.get(0).get("id").asLong()).header("Authorization",admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.slaHours").isNumber());
        JsonNode rules=mapper.readTree(mvc.perform(get("/api/catalogos/matriz-prioridad").header("Authorization",admin))
                .andReturn().getResponse().getContentAsString());
        mvc.perform(get("/api/catalogos/matriz-prioridad/"+rules.get(0).get("id").asLong()).header("Authorization",admin)).andExpect(status().isOk());
        mvc.perform(get("/api/usuarios/"+technicianId).header("Authorization",admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordHash").doesNotExist()).andExpect(jsonPath("$.password").doesNotExist());
    }
    @Test void cambioDeRolInvalidaTokenAnterior() throws Exception {
        var account=users.findByUsernameIgnoreCase("admin.prueba").orElseThrow();
        account.setRole(UserRole.SOLICITANTE); users.saveAndFlush(account);
        mvc.perform(get("/api/usuarios").header("Authorization",admin)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios").header("Authorization",token("admin.prueba"))).andExpect(status().isForbidden());
    }
    @Test void aislamientoDeSolicitanteYTecnicoAsignado() throws Exception {
        long id=createTicket().get("id").asLong();
        userService.crear(new UserRequest("otro","otro@local.test","prueba-local-123",UserRole.SOLICITANTE,areaId,true));
        String other=token("otro");
        mvc.perform(get("/api/tickets/"+id).header("Authorization",other)).andExpect(status().isForbidden());
        mvc.perform(get("/api/tickets/"+id+"/historial").header("Authorization",other)).andExpect(status().isForbidden());
        mvc.perform(get("/api/tickets").header("Authorization",other)).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(put("/api/tickets/"+id+"/estado").header("Authorization",technician)
                .contentType(MediaType.APPLICATION_JSON).content("{\"state\":\"EN_ATENCION\"}"))
                .andExpect(status().isForbidden());
    }
}
