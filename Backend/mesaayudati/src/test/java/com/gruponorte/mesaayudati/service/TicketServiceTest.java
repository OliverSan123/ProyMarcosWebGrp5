package com.gruponorte.mesaayudati.service;

import com.gruponorte.mesaayudati.dto.*;
import com.gruponorte.mesaayudati.entity.*;
import com.gruponorte.mesaayudati.repository.*;
import com.gruponorte.mesaayudati.service.impl.TicketServiceImpl;
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

/** Equivalente a ProductoServiceTest: asociación, persistencia y referencias inválidas. */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock TicketRepository tickets;
    @Mock TicketHistoryRepository history;
    @Mock UserRepository users;
    @Mock CategoryRepository categories;
    @Mock AreaRepository areas;
    @Mock PriorityRuleRepository rules;
    TicketServiceImpl service;
    @BeforeEach void setup() {
        service = new TicketServiceImpl(tickets,history,users,categories,areas,rules);
    }
    AppUser requester() {
        AppUser u=new AppUser(); ReflectionTestUtils.setField(u,"id",1L);
        u.setUsername("solicitante"); u.setRole(UserRole.SOLICITANTE);
        when(users.findByUsernameIgnoreCase("solicitante")).thenReturn(Optional.of(u)); return u;
    }
    TicketCreateRequest request(Long category) {
        return new TicketCreateRequest("  Sin red  ","  No tengo conexión  ",category,2L,ImpactLevel.ALTO,ImpactLevel.ALTO);
    }
    @Test void crearAsociaCategoriaAreaPrioridadYRegistraHistorial() {
        requester();
        Category c=new Category(); ReflectionTestUtils.setField(c,"id",3L); c.setName("Redes");
        Area a=new Area(); ReflectionTestUtils.setField(a,"id",2L); a.setName("TI");
        Priority p=new Priority(); ReflectionTestUtils.setField(p,"id",4L); p.setName("CRITICA"); p.setSlaHours(4);
        PriorityRule r=new PriorityRule(); r.setPriority(p);
        when(categories.findById(3L)).thenReturn(Optional.of(c));
        when(areas.findById(2L)).thenReturn(Optional.of(a));
        when(rules.findByImpactAndUrgency(ImpactLevel.ALTO,ImpactLevel.ALTO)).thenReturn(Optional.of(r));
        when(tickets.save(any())).thenAnswer(inv->{Ticket t=inv.getArgument(0); ReflectionTestUtils.setField(t,"id",9L); return t;});
        var result=service.crear(request(3L),"solicitante");
        assertEquals(9L,result.id()); assertEquals("Sin red",result.title());
        assertEquals(3L,result.categoryId()); assertEquals(2L,result.areaId());
        assertEquals("CRITICA",result.priority()); assertEquals("NUEVO",result.state());
        assertEquals(result.createdAt().plusHours(4),result.targetAt());
        verify(history).save(any(TicketHistory.class));
    }
    @Test void categoriaInexistenteImpideGuardar() {
        requester();
        var ex=assertThrows(ResponseStatusException.class,()->service.crear(request(99L),"solicitante"));
        assertEquals(404,ex.getStatusCode().value()); verifyNoInteractions(tickets,history);
    }
    @Test void consultaInexistenteDevuelve404() {
        requester();
        var ex=assertThrows(ResponseStatusException.class,()->service.obtener(99L,"solicitante"));
        assertEquals(404,ex.getStatusCode().value());
    }
    @Test void solicitanteNoVeTicketAjeno() {
        requester(); AppUser owner=new AppUser(); ReflectionTestUtils.setField(owner,"id",2L);
        Ticket t=new Ticket(); t.setCreator(owner); when(tickets.findById(9L)).thenReturn(Optional.of(t));
        var ex=assertThrows(ResponseStatusException.class,()->service.obtener(9L,"solicitante"));
        assertEquals(403,ex.getStatusCode().value());
    }
    @Test void cierreDeTicketNuevoSeRechaza() {
        AppUser coordinator=new AppUser(); coordinator.setRole(UserRole.COORDINADOR);
        when(users.findByUsernameIgnoreCase("coordinador")).thenReturn(Optional.of(coordinator));
        Ticket t=new Ticket(); t.setState(TicketStatus.NUEVO); when(tickets.findById(9L)).thenReturn(Optional.of(t));
        var ex=assertThrows(ResponseStatusException.class,()->service.cerrar(9L,"coordinador"));
        assertEquals(409,ex.getStatusCode().value()); verify(tickets,never()).save(any());
    }
}
