package com.example.Service;

import java.time.LocalDate;


public record DatosReserva(
        String vehiculoId,
        String correoTurista,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer pasajeros,
        String trayectos) {
}
