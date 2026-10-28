package com.example.Service;

import com.example.Model.Pago;
import com.example.Model.Reserva;
import com.example.Repository.PagoRepository;
import com.example.Repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// Pago simulado: no se cobra dinero de verdad ni se piden datos de tarjeta.
// Sprint 7: se paga el anticipo del 10% del valor estimado, por transferencia (RF-35, RF-37).
@Service
public class PagoService {

    private static final Logger LOG = LoggerFactory.getLogger(PagoService.class);

    // RF-37: transferencia desde estos bancos o PayPal
    public static final List<String> METODOS = List.of("BBVA", "BANCOLOMBIA", "BANCO_BOGOTA", "PAYPAL");

    static final double PORCENTAJE_ANTICIPO = 0.10;
    static final int TITULAR_MINIMO = 3;
    static final int TITULAR_MAXIMO = 60;

    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;

    public PagoService(ReservaRepository reservaRepository, PagoRepository pagoRepository) {
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
    }

    // El valor de todo el servicio: el final si el conductor ya lo registró; si no, el estimado
    public static Long totalDe(Reserva reserva) {
        return reserva.getValorFinal() != null ? reserva.getValorFinal() : reserva.getValorEstimado();
    }

    // Lo que se paga ahora: el 10% del valor estimado, redondeado a la centena (RF-35).
    // Si por alguna razón no hay estimado, se usa el valor final.
    public static Long montoDe(Reserva reserva) {
        Long base = reserva.getValorEstimado() != null ? reserva.getValorEstimado() : reserva.getValorFinal();
        if (base == null) {
            return null;
        }
        return CalculadoraTarifa.redondearCien(base * PORCENTAJE_ANTICIPO);
    }

    public static String etiqueta(String metodo) {
        if (metodo == null) {
            return "";
        }
        return switch (metodo) {
            case "BBVA" -> "BBVA";
            case "BANCOLOMBIA" -> "Bancolombia";
            case "BANCO_BOGOTA" -> "Banco de Bogotá";
            case "PAYPAL" -> "PayPal";
            default -> metodo;
        };
    }

    // Simula un pago exitoso, lo guarda y lo relaciona con la reserva
    public Pago pagar(String codigoReserva, String correoTurista, String metodo, String titular) {
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
            throw new IllegalArgumentException("Elige desde qué banco o PayPal haces la transferencia.");
        }
        String titularLimpio = titular == null ? "" : titular.trim();
        if (titularLimpio.length() < TITULAR_MINIMO || titularLimpio.length() > TITULAR_MAXIMO) {
            throw new IllegalArgumentException("Escribe el nombre del titular de la cuenta (entre "
                    + TITULAR_MINIMO + " y " + TITULAR_MAXIMO + " letras).");
        }
        Long monto = montoDe(reserva);
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("Esta reserva no tiene un valor para pagar.");
        }

        Pago pago = new Pago(referenciaNueva(), reserva.getCodigo(), reserva.getCorreoTurista(),
                monto, metodoElegido);
        pago.setTitular(titularLimpio);
        pagoRepository.save(pago);

        // Relación en los dos sentidos: el pago guarda el código de la reserva y la reserva guarda la referencia
        reserva.setEstadoPago(Reserva.PAGADA);
        reserva.setReferenciaPago(pago.getReferencia());
        reservaRepository.save(reserva);

        // RF-39 (simulado): no hay correo real; el recibo queda escrito en el registro del servidor
        LOG.info("Recibo simulado enviado a {}: {}", reserva.getCorreoTurista(), textoRecibo(pago, reserva));
        return pago;
    }

    // El texto del recibo que se le "enviaría" al turista
    static String textoRecibo(Pago pago, Reserva reserva) {
        return "Recibo " + pago.getReferencia() + ": recibimos " + TarifaEstimada.pesos(pago.getMonto())
                + " de " + pago.getTitular() + " por " + etiqueta(pago.getMetodo())
                + ", anticipo del 10% de la reserva " + reserva.getCodigo()
                + " (del " + reserva.getFechaInicio() + " al " + reserva.getFechaFin() + ").";
    }

    private String referenciaNueva() {
        return "PAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
