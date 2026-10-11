package com.example.Service;

import com.example.Model.PeriodoOcupado;
import com.example.Model.Vehiculo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehiculoServiceTest {

   
    private final VehiculoService servicio = new VehiculoService(null);

    private final LocalDate inicio = LocalDate.of(2026, 8, 14);
    private final LocalDate fin = LocalDate.of(2026, 8, 19);

    private Vehiculo vehiculo(String id, String gama, int asientos) {
        Vehiculo v = new Vehiculo("conductor@correo.com", "Modelo " + id, gama, asientos);
        v.setId(id);
        return v;
    }

    @Test
    void vehiculoSinReservasEstaLibre() {
        assertTrue(servicio.estaLibre(vehiculo("1", "Económico", 4), inicio, fin));
    }

    @Test
    void vehiculoApagadoNoEstaLibre() {
        Vehiculo v = vehiculo("1", "Económico", 4);
        v.setDisponible(false);
        assertFalse(servicio.estaLibre(v, inicio, fin));
    }

    @Test
    void reservaQueSeCruzaNoEstaLibre() {
        Vehiculo v = vehiculo("1", "Económico", 4);
        v.getPeriodosOcupados().add(
                new PeriodoOcupado(LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 22)));
        assertFalse(servicio.estaLibre(v, inicio, fin));
    }

    @Test
    void reservaQueTerminaElDiaQueEmpiezaNoEstaLibre() {
        Vehiculo v = vehiculo("1", "Económico", 4);
        v.getPeriodosOcupados().add(
                new PeriodoOcupado(LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 14)));
        assertFalse(servicio.estaLibre(v, inicio, fin));
    }

    @Test
    void reservaEnOtrasFechasSiEstaLibre() {
        Vehiculo v = vehiculo("1", "Económico", 4);
        v.getPeriodosOcupados().add(
                new PeriodoOcupado(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5)));
        assertTrue(servicio.estaLibre(v, inicio, fin));
    }

    @Test
    void fechasInvalidasNoEstaLibre() {
        Vehiculo v = vehiculo("1", "Económico", 4);
        assertFalse(servicio.estaLibre(v, fin, inicio)); // fin antes de inicio
        assertFalse(servicio.estaLibre(v, null, fin));
    }

    @Test
    void sugiereLosDeLaMismaGamaPrimero() {
        Vehiculo pedido = vehiculo("1", "Económico", 4);
        Vehiculo otraGama = vehiculo("2", "Ejecutivo", 4);
        Vehiculo mismaGama = vehiculo("3", "Económico", 5);

        List<Vehiculo> r = servicio.filtrarSimilares(
                pedido, List.of(pedido, otraGama, mismaGama), inicio, fin);

        assertEquals(2, r.size());
        assertEquals("3", r.get(0).getId());
        assertEquals("2", r.get(1).getId());
    }

    @Test
    void noSugiereElMismoNiLosQueNoCaben() {
        Vehiculo pedido = vehiculo("1", "Van", 7);
        Vehiculo pequeno = vehiculo("2", "Van", 4);

        List<Vehiculo> r = servicio.filtrarSimilares(
                pedido, List.of(pedido, pequeno), inicio, fin);

        assertTrue(r.isEmpty());
    }

    @Test
    void noSugiereLosOcupadosEnEsasFechas() {
        Vehiculo pedido = vehiculo("1", "Económico", 4);
        Vehiculo ocupado = vehiculo("2", "Económico", 4);
        ocupado.getPeriodosOcupados().add(
                new PeriodoOcupado(LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 16)));

        List<Vehiculo> r = servicio.filtrarSimilares(
                pedido, List.of(pedido, ocupado), inicio, fin);

        assertTrue(r.isEmpty());
    }
}
