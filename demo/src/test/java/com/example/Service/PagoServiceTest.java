package com.example.Service;

import com.example.Model.Pago;
import com.example.Model.Reserva;
import com.example.Repository.PagoRepository;
import com.example.Repository.ReservaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Sprint 7 (H15): pago simulado del anticipo (10% del estimado) por transferencia
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

    private String error(String correo, String metodo, String titular) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.pagar("AND-1234", correo, metodo, titular));
        return e.getMessage();
    }

    @Test
    void pagarGuardaElPagoRelacionadoConLaReservaYLaMarcaPagada() {
        Reserva r = reserva(300000L);

        Pago pago = servicio.pagar("AND-1234", "ana@x.com", "bbva", "Ana Gómez");

        assertEquals("AND-1234", pago.getReservaCodigo());
        assertEquals(30000L, pago.getMonto());
        assertEquals("BBVA", pago.getMetodo());
        assertEquals("Ana Gómez", pago.getTitular());
        assertEquals(Pago.EXITOSO, pago.getEstado());
        assertTrue(pago.getReferencia().startsWith("PAG-"));
        assertEquals(Reserva.PAGADA, r.getEstadoPago());
        assertEquals(pago.getReferencia(), r.getReferenciaPago());
        verify(pagoRepository).save(pago);
        verify(reservaRepository).save(r);
    }

    @Test
    void elAnticipoEsElDiezPorCientoDelEstimadoRedondeadoALaCentena() {
        assertEquals(30000L, PagoService.montoDe(reserva(300000L)));
        assertEquals(34600L, PagoService.montoDe(reserva(345678L)));
        assertEquals(1000L, PagoService.montoDe(reserva(10000L)));
    }

    @Test
    void aunqueExistaValorFinalElAnticipoSigueSiendoElDiezPorCientoDelEstimado() {
        Reserva r = reserva(300000L);
        r.setValorFinal(340000L);

        assertEquals(30000L, PagoService.montoDe(r));
        assertEquals(340000L, PagoService.totalDe(r));
    }

    @Test
    void sinEstimadoSeUsaElValorFinal() {
        Reserva r = reserva(null);
        r.setValorFinal(200000L);
        assertEquals(20000L, PagoService.montoDe(r));
    }

    @Test
    void cadaMetodoDeLaListaSeAcepta() {
        for (String metodo : PagoService.METODOS) {
            reserva(300000L);
            Pago pago = servicio.pagar("AND-1234", "ana@x.com", metodo, "Ana Gómez");
            assertEquals(metodo, pago.getMetodo());
        }
        assertEquals(4, PagoService.METODOS.size());
    }

    @Test
    void losMetodosViejosYaNoSeAceptan() {
        reserva(300000L);
        assertTrue(error("ana@x.com", "TARJETA", "Ana Gómez").contains("banco o PayPal"));
        assertTrue(error("ana@x.com", "NEQUI", "Ana Gómez").contains("banco o PayPal"));
        assertTrue(error("ana@x.com", null, "Ana Gómez").contains("banco o PayPal"));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void sinTitularNoSePaga() {
        reserva(300000L);
        assertTrue(error("ana@x.com", "BBVA", null).contains("titular"));
        assertTrue(error("ana@x.com", "BBVA", "   ").contains("titular"));
        assertTrue(error("ana@x.com", "BBVA", "Al").contains("titular"));
        assertTrue(error("ana@x.com", "BBVA", "a".repeat(61)).contains("titular"));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void elTitularSeGuardaSinEspaciosSobrantes() {
        reserva(300000L);
        Pago pago = servicio.pagar("AND-1234", "ana@x.com", "PAYPAL", "  Ana Gómez  ");
        assertEquals("Ana Gómez", pago.getTitular());
    }

    @Test
    void unaReservaYaPagadaNoSePagaDeNuevo() {
        Reserva r = reserva(300000L);
        r.setEstadoPago(Reserva.PAGADA);
        assertTrue(error("ana@x.com", "BBVA", "Ana Gómez").contains("ya está pagada"));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void soloElDuenoDeLaReservaPuedePagar() {
        reserva(300000L);
        assertTrue(error("otro@x.com", "BBVA", "Ana Gómez").contains("no es tuya"));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void unaReservaCanceladaNoSePaga() {
        Reserva r = reserva(300000L);
        r.setEstado("CANCELADA");
        assertTrue(error("ana@x.com", "BBVA", "Ana Gómez").contains("reservas confirmadas"));
    }

    @Test
    void unaReservaSinValorNoSePuedePagar() {
        reserva(null);
        assertNull(PagoService.montoDe(reserva(null)));
        assertTrue(error("ana@x.com", "BBVA", "Ana Gómez").contains("no tiene un valor"));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void unaReservaQueNoExisteLanzaError() {
        when(reservaRepository.findByCodigo("nada")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> servicio.pagar("nada", "ana@x.com", "BBVA", "Ana Gómez"));
    }

    @Test
    void lasEtiquetasSonLasQueVeElTurista() {
        assertEquals("BBVA", PagoService.etiqueta("BBVA"));
        assertEquals("Bancolombia", PagoService.etiqueta("BANCOLOMBIA"));
        assertEquals("Banco de Bogotá", PagoService.etiqueta("BANCO_BOGOTA"));
        assertEquals("PayPal", PagoService.etiqueta("PAYPAL"));
    }

    @Test
    void elReciboLlevaLosDatosDelPago() {
        Reserva r = reserva(300000L);
        Pago pago = servicio.pagar("AND-1234", "ana@x.com", "BANCO_BOGOTA", "Ana Gómez");

        String recibo = PagoService.textoRecibo(pago, r);

        assertTrue(recibo.contains(pago.getReferencia()));
        assertTrue(recibo.contains("$30.000"));
        assertTrue(recibo.contains("Ana Gómez"));
        assertTrue(recibo.contains("Banco de Bogotá"));
        assertTrue(recibo.contains("AND-1234"));
    }
}
