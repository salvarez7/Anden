package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.Gama;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.VehiculoRepository;
import org.junit.jupiter.api.Test;

import com.example.Model.PeriodoOcupado;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogoVehiculosServiceTest {

    private final VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
    private final ConductorRepository conductorRepository = mock(ConductorRepository.class);
    private final CatalogoVehiculosService servicio =
            new CatalogoVehiculosService(vehiculoRepository, conductorRepository,
                    new VehiculoService(vehiculoRepository));

    private Vehiculo vehiculo(String correo, String gama, int asientos) {
        return new Vehiculo(correo, "Carro de " + correo, gama, asientos);
    }

    private void conductorCon(String correo, String estado) {
        Conductor c = new Conductor();
        c.setCorreo(correo);
        c.setNombre("Conductor " + correo);
        c.setEstado(estado);
        when(conductorRepository.findByCorreo(correo)).thenReturn(Optional.of(c));
    }

    @Test
    void sinFiltrosBuscaTodosDesdeUnAsiento() {
        conductorCon("a@x.com", "APROBADO");
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(1))
                .thenReturn(List.of(vehiculo("a@x.com", "economica", 4)));

        List<ResultadoBusqueda> r = servicio.buscar(null, null);

        assertEquals(1, r.size());
    }

    @Test
    void filtraPorGamaYAsientos() {
        conductorCon("a@x.com", "APROBADO");
        when(vehiculoRepository.findByGamaAndNumeroAsientosGreaterThanEqual("premium", 5))
                .thenReturn(List.of(vehiculo("a@x.com", "premium", 7)));

        List<ResultadoBusqueda> r = servicio.buscar("Premium", 5);

        assertEquals(1, r.size());
        assertEquals("Premium", r.get(0).etiquetaGama());
    }

    @Test
    void gamaDesconocidaNoFiltraPorGama() {
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(1)).thenReturn(List.of());

        servicio.buscar("voladora", 0);

        verify(vehiculoRepository).findByNumeroAsientosGreaterThanEqual(1);
    }

    @Test
    void noMuestraConductoresPendientesNiVehiculosApagados() {
        conductorCon("aprobado@x.com", "APROBADO");
        conductorCon("pendiente@x.com", "PENDIENTE_VERIFICACION");
        Vehiculo apagado = vehiculo("aprobado@x.com", "estandar", 6);
        apagado.setDisponible(false);
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(1)).thenReturn(List.of(
                vehiculo("pendiente@x.com", "estandar", 4), apagado));

        assertTrue(servicio.buscar("", null).isEmpty());
    }

    @Test
    void ordenaDeMenosAMasAsientos() {
        conductorCon("a@x.com", "APROBADO");
        conductorCon("b@x.com", "APROBADO");
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(5)).thenReturn(List.of(
                vehiculo("a@x.com", "premium", 8), vehiculo("b@x.com", "estandar", 5)));

        List<ResultadoBusqueda> r = servicio.buscar(null, 5);

        assertEquals(5, r.get(0).vehiculo().getNumeroAsientos());
        assertEquals(8, r.get(1).vehiculo().getNumeroAsientos());
    }

    @Test
    void registrarVehiculoGuardaMarcaYModeloJuntos() {
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(i -> i.getArgument(0));

        Vehiculo v = servicio.registrarVehiculoDeConductor("Juan@Correo.com ", "Renault", "Duster",
                Gama.ESTANDAR, 5);

        assertEquals("Renault Duster", v.getModelo());
        assertEquals("estandar", v.getGama());
        assertEquals("juan@correo.com", v.getCorreoConductor());
    }

    @Test
    void gamaDesdeTexto() {
        assertEquals(Gama.ECONOMICA, Gama.desde(" ECONOMICA "));
        assertEquals(null, Gama.desde("van"));
        assertEquals(null, Gama.desde(null));
    }

    // ---------- Sprint 3: fechas ----------

    private final LocalDate inicio = LocalDate.now().plusDays(10);
    private final LocalDate fin = inicio.plusDays(2);

    private Vehiculo ocupadoEntre(String correo, LocalDate desde, LocalDate hasta) {
        Vehiculo v = vehiculo(correo, "estandar", 4);
        v.getPeriodosOcupados().add(new PeriodoOcupado(desde, hasta));
        return v;
    }

    @Test
    void conductoresDisponiblesQuitaLosQueTienenReservaQueSeCruza() {
        conductorCon("libre@x.com", "APROBADO");
        conductorCon("cruza@x.com", "APROBADO");
        conductorCon("antes@x.com", "APROBADO");
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(1)).thenReturn(List.of(
                vehiculo("libre@x.com", "economica", 4),
                ocupadoEntre("cruza@x.com", fin, fin.plusDays(5)),             // el último día choca
                ocupadoEntre("antes@x.com", inicio.minusDays(5), inicio.minusDays(1)))); // termina justo antes

        List<ResultadoBusqueda> r = servicio.conductoresDisponibles(inicio, fin);

        assertEquals(2, r.size());
        assertTrue(r.stream().noneMatch(x -> x.conductor().getCorreo().equals("cruza@x.com")));
    }

    @Test
    void sinFechasNoMiraLasReservas() {
        conductorCon("cruza@x.com", "APROBADO");
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(1))
                .thenReturn(List.of(ocupadoEntre("cruza@x.com", inicio, fin)));

        assertEquals(1, servicio.buscar(null, null, null, null).size());
    }

    @Test
    void conductorNoAprobadoNoSaleAunqueEsteLibre() {
        conductorCon("nuevo@x.com", "PENDIENTE_VERIFICACION");
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(1))
                .thenReturn(List.of(vehiculo("nuevo@x.com", "economica", 4)));

        assertTrue(servicio.conductoresDisponibles(inicio, fin).isEmpty());
    }

    @Test
    void validarFechasAceptaFechasBienOVacias() {
        assertTrue(servicio.validarFechas(inicio, fin).isEmpty());
        assertTrue(servicio.validarFechas(inicio, inicio).isEmpty()); // un solo día también vale
        assertTrue(servicio.validarFechas(null, null).isEmpty());
    }

    @Test
    void validarFechasRechazaFechasMalas() {
        assertEquals(List.of("Elige las dos fechas: desde y hasta."), servicio.validarFechas(inicio, null));
        assertEquals(List.of("La fecha de inicio no puede estar en el pasado."),
                servicio.validarFechas(LocalDate.now().minusDays(1), fin));
        assertEquals(List.of("La fecha final debe ser igual o posterior a la de inicio."),
                servicio.validarFechas(fin, inicio));
    }
}
