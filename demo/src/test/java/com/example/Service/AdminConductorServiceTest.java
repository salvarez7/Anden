package com.example.Service;

import com.example.Model.Conductor;
import com.example.Repository.ConductorRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminConductorServiceTest {

    private final ConductorRepository repositorio = mock(ConductorRepository.class);
    private final AdminConductorService servicio = new AdminConductorService(repositorio);

    private Conductor pendiente() {
        Conductor c = new Conductor();
        c.setId("c1");
        c.setCorreo("juan@x.com");
        c.setEstado("PENDIENTE_VERIFICACION");
        when(repositorio.findById("c1")).thenReturn(Optional.of(c));
        when(repositorio.save(any(Conductor.class))).thenAnswer(i -> i.getArgument(0));
        return c;
    }

    @Test
    void aprobarCambiaElEstadoAAprobado() {
        Conductor c = pendiente();

        Conductor resultado = servicio.decidir("c1", "APROBAR");

        assertEquals("APROBADO", resultado.getEstado());
        verify(repositorio).save(c);
    }

    @Test
    void rechazarCambiaElEstadoARechazado() {
        Conductor c = pendiente();

        Conductor resultado = servicio.decidir("c1", "RECHAZAR");

        assertEquals("RECHAZADO", resultado.getEstado());
        verify(repositorio).save(c);
    }

    @Test
    void unaDecisionInvalidaNoCambiaNadaNiGuarda() {
        Conductor c = pendiente();

        assertThrows(IllegalArgumentException.class, () -> servicio.decidir("c1", "TAL VEZ"));

        assertEquals("PENDIENTE_VERIFICACION", c.getEstado());
        verify(repositorio, never()).save(any(Conductor.class));
    }

    @Test
    void conductorQueNoExisteLanzaError() {
        when(repositorio.findById("nada")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicio.decidir("nada", "APROBAR"));
    }

    @Test
    void listarPorEstadoFiltraYSinEstadoDevuelveTodos() {
        Conductor a = new Conductor();
        when(repositorio.findByEstado("APROBADO")).thenReturn(List.of(a));
        when(repositorio.findAll()).thenReturn(List.of(a, new Conductor()));

        assertEquals(1, servicio.listar("APROBADO").size());
        assertEquals(2, servicio.listar(null).size());
        assertEquals(2, servicio.listar(" ").size());
    }
}