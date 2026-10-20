package com.example.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;


public final class WhatsAppEnlace {

    private WhatsAppEnlace() {
    }

    
    public static String crear(String telefono, String mensaje) {
        String numero = RegistroService.normalizarTelefono(telefono);
        if (!numero.matches("^3\\d{9}$")) {
            return null;
        }
        String texto = URLEncoder.encode(mensaje == null ? "" : mensaje, StandardCharsets.UTF_8)
                .replace("+", "%20"); // URLEncoder usa + para el espacio, wa.me quiere %20
        return "https://wa.me/57" + numero + "?text=" + texto;
    }


    public static String mensajeReserva(String nombreTurista, String modeloVehiculo,
                                        LocalDate inicio, LocalDate fin) {
        return mensajeReserva(nombreTurista, modeloVehiculo, inicio, fin, null, null);
    }

    public static String mensajeReserva(String nombreTurista, String modeloVehiculo,
                                        LocalDate inicio, LocalDate fin,
                                        Integer pasajeros, String trayectos) {
        StringBuilder sb = new StringBuilder("Hola, soy ");
        sb.append(nombreTurista == null || nombreTurista.isBlank() ? "un turista" : nombreTurista.trim());
        sb.append(" y vi tu vehículo");
        if (modeloVehiculo != null && !modeloVehiculo.isBlank()) {
            sb.append(" (").append(modeloVehiculo.trim()).append(")");
        }
        sb.append(" en Andén.");
        if (inicio != null && fin != null) {
            sb.append(" Quiero reservar del ").append(inicio).append(" al ").append(fin).append(".");
        }
        if (pasajeros != null && pasajeros > 0) {
            sb.append(" Pasajeros: ").append(pasajeros).append(".");
        }
        if (trayectos != null && !trayectos.isBlank()) {
            String t = trayectos.trim();
            sb.append(" Trayectos: ").append(t).append(t.endsWith(".") ? "" : ".");
        }
        sb.append(" ¿Podemos hablar de mis planes y horarios?");
        return sb.toString();
    }
}
