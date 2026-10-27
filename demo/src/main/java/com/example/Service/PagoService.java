package com.example.Service;

import com.example.Model.Pago;
import com.example.Model.Reserva;
import com.example.Repository.PagoRepository;
import com.example.Repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// Pago simulado: no se cobra dinero de verdad ni se piden datos de tarjeta
@Service
public class PagoService {

    public static final List<String> METODOS = List.of("TARJETA", "PSE", "NEQUI");

    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;

    public PagoService(ReservaRepository reservaRepository, PagoRepository pagoRepository) {
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
    }

    // Lo que se paga: el valor final si el conductor ya lo registró; si no, el estimado
    public static Long montoDe(Reserva reserva) {
        return reserva.getValorFinal() != null ? reserva.getValorFinal() : reserva.getValorEstimado();
    }

    // Simula un pago exitoso, lo guarda y lo relaciona con la reserva
    public Pago pagar(String codigoReserva, String correoTurista, String metodo) {
        Reserva reserva = reservaRepository.findByCodigo(codigoReserva)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos esa reserva."));

        if (correoTurista == null || !correoTurista.equals(reserva.getCorreoTurista())) {
            throw new IllegalArgumentException("Esta reserva no es tuya.");
        }
        if (!Reserva.CONFIRMADA.equals(reserva.getEstado())) {
            throw new IllegalArgumentException("Solo se pagan reservas confirmadas.");
        }
        if (Reserva.PAGADA.equals(reserva.getEstadoPago())
                || pagoRepository.findByReservaCodigo(reserva.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("Esta reserva ya está pagada.");
        }

        String metodoElegido = metodo == null ? "" : metodo.trim().toUpperCase();
        if (!METODOS.contains(metodoElegido)) {
            throw new IllegalArgumentException("Elige un método de pago.");
        }

        Long monto = montoDe(reserva);
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("Esta reserva no tiene un valor para pagar.");
        }

        Pago pago = new Pago(referenciaNueva(), reserva.getCodigo(), reserva.getCorreoTurista(),
                monto, metodoElegido);
        pagoRepository.save(pago);

        // Relación en los dos sentidos: el pago guarda el código de la reserva y la reserva guarda la referencia
        reserva.setEstadoPago(Reserva.PAGADA);
        reserva.setReferenciaPago(pago.getReferencia());
        reservaRepository.save(reserva);
        return pago;
    }

    private String referenciaNueva() {
        return "PAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}