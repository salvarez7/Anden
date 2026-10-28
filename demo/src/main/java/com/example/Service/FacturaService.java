package com.example.Service;

import com.example.Model.Factura;
import com.example.Model.Pago;
import com.example.Model.Reserva;
import com.example.Repository.FacturaRepository;
import com.example.Repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

// psra generar la factura simulada cuando la reserva ya está paga
@Service
public class FacturaService {

    public static final int PORCENTAJE_ANTICIPO = 10;

    public static final String AVISO = "Factura simulada: Andén no procesa pagos reales. "
            + "El saldo se le paga directamente al conductor al terminar el viaje.";

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;

    public FacturaService(FacturaRepository facturaRepository, PagoRepository pagoRepository) {
        this.facturaRepository = facturaRepository;
        this.pagoRepository = pagoRepository;
    }


    public static long saldo(Reserva reserva, Factura factura) {
        Long total = PagoService.totalDe(reserva);
        return total == null ? 0 : Math.max(0, total - factura.getValorPagado());
    }


    public Optional<Factura> facturaDe(Reserva reserva) {
        Optional<Factura> existente = facturaRepository.findByCodigoReserva(reserva.getCodigo());
        if (existente.isPresent()) {
            return existente;
        }
        if (!Reserva.PAGADA.equals(reserva.getEstadoPago())) {
            return Optional.empty();
        }
        Optional<Pago> pago = pagoRepository.findByReservaCodigo(reserva.getCodigo());
        if (pago.isEmpty()) {
            return Optional.empty();
        }
        Pago p = pago.get();
        long valorEstimado = reserva.getValorEstimado() == null ? 0 : reserva.getValorEstimado();
        Factura factura = new Factura("FAC-" + reserva.getCodigo(), reserva.getCodigo(), reserva.getCorreoTurista(),
                valorEstimado, PORCENTAJE_ANTICIPO, p.getMonto(), PagoService.etiqueta(p.getMetodo()),
                p.getTitular(), p.getReferencia(), p.getFecha());
        return Optional.of(facturaRepository.save(factura));
    }
}
