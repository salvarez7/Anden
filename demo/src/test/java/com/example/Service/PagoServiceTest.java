package com.example.Service;

import com.example.Model.Pago;
import com.example.Model.Reserva;
import com.example.Repository.PagoRepository;
import com.example.Repository.ReservaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PagoServiceTest {

    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private final PagoRepository pagoRepository = mock(PagoRepository.class);
    private final PagoService servicio = new PagoService(reservaRepository, pagoRepository);

    private Reserva reserva(Long valorEstimado) {
        LocalDate inicio = LocalDate.now().plusDays(10);
        Reserva r = new Reserva("AND-1234", "v1", "juan@x.com", "ana@x.com",
                inicio, inicio.plusDays(3), 3, "Aeropuerto a hotel");
        r.setValorEstimado(valorEstimado);
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(r));
        return r;
    }

    @Test
    void pagarGuardaElPagoRelacionadoConLaReservaYLaMarcaPagada() {
        Reserva r = reserva(300000L);

        Pago pago = servicio.pagar("AND-1234", "ana@x.com", "tarjeta");

        assertEquals("AND-1234", pago.getReservaCodigo());
        assertEquals(300000L, pago.getMonto());
        assertEquals("TARJETA", pago.getMetodo());
        assertEquals(Pago.EXITOSO, pago.getEstado());
        assertTrue(pago.getReferencia().startsWith("PAG-"));
        assertEquals(Reserva.PAGADA, r.getEstadoPago());
        assertEquals(pago.getReferencia(), r.getReferenciaPago());
        verify(pagoRepository).save(pago);
        verify(reservaRepository).save(r);
    }

    @Test
    void siElConductorYaRegistroElValorFinalSePagaElFinal() {
        Reserva r = reserva(300000L);
        r.setValorFinal(340000L);

        Pago pago = servicio.pagar("AND-1234", "ana@x.com", "PSE");

        assertEquals(340000L, pago.getMonto());
    }

    @Test
    void unaReservaYaPagadaNoSePagaDeNuevo() {
        Reserva r = reserva(300000L);
        r.setEstadoPago(Reserva.PAGADA);

        assertThrows(IllegalArgumentException.class, () -> servicio.pagar("AND-1234", "ana@x.com", "PSE"));

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void soloElDuenoDeLaReservaPuedePagar() {
        reserva(300000L);

        assertThrows(IllegalArgumentException.class, () -> servicio.pagar("AND-1234", "otro@x.com", "PSE"));

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void unaReservaSinValorNoSePuedePagar() {
        reserva(null);

        assertThrows(IllegalArgumentException.class, () -> servicio.pagar("AND-1234", "ana@x.com", "PSE"));

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void unMetodoInvalidoSeRechaza() {
        reserva(300000L);

        assertThrows(IllegalArgumentException.class, () -> servicio.pagar("AND-1234", "ana@x.com", "BITCOIN"));
        assertThrows(IllegalArgumentException.class, () -> servicio.pagar("AND-1234", "ana@x.com", null));
    }

    @Test
    void unaReservaQueNoExisteLanzaError() {
        when(reservaRepository.findByCodigo("nada")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicio.pagar("nada", "ana@x.com", "PSE"));
    }
}