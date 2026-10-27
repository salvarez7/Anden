package com.example.Service;

import com.example.Model.Reserva;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.ReservaRepository;
import com.example.Repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ValorFinalServiceTest {

    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private final VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
    private final ConductorRepository conductorRepository = mock(ConductorRepository.class);

    private final ReservaService servicio = new ReservaService(
            reservaRepository, vehiculoRepository, conductorRepository,
            new VehiculoService(vehiculoRepository));

    private final LocalDate hoy = LocalDate.of(2027, 8, 20);
    private Reserva reserva;

    @BeforeEach
    void preparar() {
       
        Vehiculo duster = new Vehiculo("juan@correo.com", "Renault Duster", "estandar", 5);
        duster.setId("v1");
        reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                LocalDate.of(2027, 8, 18), LocalDate.of(2027, 8, 20), 3, "Aeropuerto a hotel");
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(duster));
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(reserva));
    }

    private String error(String correoConductor, Integer km, Integer horas, LocalDate dia) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarValorFinal("AND-1234", correoConductor, km, horas, dia));
        return e.getMessage();
    }

    @Test
    void registraElValorFinalCuandoTerminaElServicio() {
        // Estándar: 8 h * 12.000 + 100 km * 1.450 = 241.000
        Reserva r = servicio.registrarValorFinal("AND-1234", "juan@correo.com", 100, 8, hoy);

        assertEquals(241_000L, r.getValorFinal());
        assertEquals(100, r.getKilometrosReales());
        assertEquals(8, r.getHorasReales());
        assertNotNull(r.getFechaValorFinal());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void antesDeQueTermineElServicioNoSePuedeRegistrar() {
        String error = error("juan@correo.com", 100, 8, LocalDate.of(2027, 8, 19));
        assertTrue(error.contains("cuando termina el servicio"));
        verify(reservaRepository, never()).save(any());
        assertNull(reserva.getValorFinal());
    }

    @Test
    void elMismoDiaQueTerminaYaSePuedeRegistrar() {
        Reserva r = servicio.registrarValorFinal("AND-1234", "juan@correo.com", 100, 8, LocalDate.of(2027, 8, 20));
        assertNotNull(r.getValorFinal());
    }

    @Test
    void variosDiasDespuesDeQueTerminoTambienSePuedeRegistrar() {
        Reserva r = servicio.registrarValorFinal("AND-1234", "juan@correo.com", 100, 8, LocalDate.of(2027, 8, 25));
        assertNotNull(r.getValorFinal());
    }

    @Test
    void sePuedeActualizarElValorFinal() {
        servicio.registrarValorFinal("AND-1234", "juan@correo.com", 100, 8, hoy);
       
        Reserva r = servicio.registrarValorFinal("AND-1234", "juan@correo.com", 180, 10, hoy);

        assertEquals(381_000L, r.getValorFinal());
        assertEquals(180, r.getKilometrosReales());
        verify(reservaRepository, times(2)).save(reserva);
    }

    @Test
    void laDiferenciaConElEstimadoSeCalcula() {
        reserva.setValorEstimado(300_000L);
        servicio.registrarValorFinal("AND-1234", "juan@correo.com", 100, 8, hoy);
        assertEquals(-59_000L, reserva.getDiferenciaConEstimado());

        servicio.registrarValorFinal("AND-1234", "juan@correo.com", 180, 10, hoy);
        assertEquals(81_000L, reserva.getDiferenciaConEstimado());
    }

    @Test
    void sinEstimadoNoHayDiferencia() {
        servicio.registrarValorFinal("AND-1234", "juan@correo.com", 100, 8, hoy);
        assertNull(reserva.getDiferenciaConEstimado());
    }

    @Test
    void elValorIncluyeElRecargoPorPasajerosExtra() {
        reserva.setPasajeros(5);
    
        Reserva r = servicio.registrarValorFinal("AND-1234", "juan@correo.com", 0, 12, hoy);
        assertEquals(159_000L, r.getValorFinal());
    }

    @Test
    void unaReservaDeOtroConductorNoSePuedeTocar() {
        assertTrue(error("luis@correo.com", 100, 8, hoy).contains("no es de tu vehículo"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void unaReservaCanceladaNoTieneValorFinal() {
        reserva.setEstado("CANCELADA");
        assertTrue(error("juan@correo.com", 100, 8, hoy).contains("reservas confirmadas"));
    }

    @Test
    void kilometrosNegativosNoSeGuardan() {
        assertTrue(error("juan@correo.com", -5, 8, hoy).contains("kilómetros"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void horasEnCeroNoSeGuardan() {
        assertTrue(error("juan@correo.com", 100, 0, hoy).contains("horas"));
    }

    @Test
    void siFaltanLosDosDatosSeMuestranLosDosMensajes() {
        String error = error("juan@correo.com", null, null, hoy);
        assertTrue(error.contains("kilómetros"));
        assertTrue(error.contains("horas"));
    }

    @Test
    void unaReservaQueNoExisteLanzaError() {
        when(reservaRepository.findByCodigo("AND-0000")).thenReturn(Optional.empty());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarValorFinal("AND-0000", "juan@correo.com", 100, 8, hoy));
        assertTrue(e.getMessage().contains("No encontramos esa reserva"));
    }

    @Test
    void sinGamaEnElVehiculoNoSePuedeCalcular() {
        Vehiculo sinGama = new Vehiculo("juan@correo.com", "Raro", "otra", 4);
        sinGama.setId("v1");
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(sinGama));
        assertTrue(error("juan@correo.com", 100, 8, hoy).contains("falta la gama"));
    }
}
