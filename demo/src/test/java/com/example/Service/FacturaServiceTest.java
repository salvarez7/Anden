package com.example.Service;

import com.example.Model.Factura;
import com.example.Model.Pago;
import com.example.Model.Reserva;
import com.example.Repository.FacturaRepository;
import com.example.Repository.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FacturaServiceTest {

    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final PagoRepository pagoRepository = mock(PagoRepository.class);
    private final FacturaService servicio = new FacturaService(facturaRepository, pagoRepository);

    private Reserva reserva;
    private Pago pago;

    @BeforeEach
    void preparar() {
        reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                LocalDate.of(2027, 7, 21), LocalDate.of(2027, 7, 24), 3, "Aeropuerto a hotel");
        reserva.setValorEstimado(542_300L);
        reserva.setEstadoPago(Reserva.PAGADA);
        reserva.setReferenciaPago("PAG-1A2B3C4D");
        pago = new Pago("PAG-1A2B3C4D", "AND-1234", "ana@correo.com", PagoService.montoDe(reserva), "BANCOLOMBIA");
        pago.setTitular("Ana Gómez");
        when(facturaRepository.findByCodigoReserva("AND-1234")).thenReturn(Optional.empty());
        when(pagoRepository.findByReservaCodigo("AND-1234")).thenReturn(Optional.of(pago));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void conLaReservaPagadaGeneraLaFacturaConLosDatosDelPago() {
        Factura f = servicio.facturaDe(reserva).orElseThrow();

        assertEquals("FAC-AND-1234", f.getNumero());
        assertEquals("AND-1234", f.getCodigoReserva());
        assertEquals("ana@correo.com", f.getCorreoTurista());
        assertEquals(542_300, f.getValorEstimado());
        assertEquals(10, f.getPorcentaje());
        assertEquals(54_200, f.getValorPagado());       // el 10 % que se pagó (PagoService lo redondea a cientos)
        assertEquals("Bancolombia", f.getMetodoPago());
        assertEquals("Ana Gómez", f.getTitular());
        assertEquals("PAG-1A2B3C4D", f.getReferenciaPago());
        assertNotNull(f.getFechaEmision());
        verify(facturaRepository).save(f);
    }

    @Test
    void siYaTieneFacturaDevuelveLaMismaSinGuardarOtra() {
        Factura existente = new Factura("FAC-AND-1234", "AND-1234", "ana@correo.com", 542_300, 10, 54_200,
                "Bancolombia", "Ana Gómez", "PAG-1A2B3C4D", null);
        when(facturaRepository.findByCodigoReserva("AND-1234")).thenReturn(Optional.of(existente));

        assertSame(existente, servicio.facturaDe(reserva).orElseThrow());
        verify(facturaRepository, never()).save(any());
    }

    @Test
    void sinPagarNoHayFactura() {
        reserva.setEstadoPago(null);

        assertTrue(servicio.facturaDe(reserva).isEmpty());
        verify(facturaRepository, never()).save(any());
    }

    @Test
    void sinElRegistroDelPagoNoHayFactura() {
        when(pagoRepository.findByReservaCodigo("AND-1234")).thenReturn(Optional.empty());

        assertTrue(servicio.facturaDe(reserva).isEmpty());
    }

    @Test
    void elSaldoUsaElValorFinalSiYaExiste() {
        Factura f = servicio.facturaDe(reserva).orElseThrow();
        assertEquals(488_100, FacturaService.saldo(reserva, f));   // 542.300 - 54.200

        reserva.setValorFinal(600_000L);
        assertEquals(545_800, FacturaService.saldo(reserva, f));   // 600.000 - 54.200
    }
}
