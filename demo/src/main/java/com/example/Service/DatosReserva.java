package com.example.Service;

import java.time.LocalDate;


public record DatosReserva(
        String vehiculoId,
        String correoTurista,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer pasajeros,
        String trayectos,
        Integer numeroTrayectos,
        Integer trayectosFuera,
        Integer horasPorDia,
        Integer horaInicio) {

    // Sprint 6: sin los datos de la tarifa, para que el código anterior siga funcionando (no se calcula estimado)
    public DatosReserva(String vehiculoId, String correoTurista, LocalDate fechaInicio, LocalDate fechaFin,
                        Integer pasajeros, String trayectos) {
        this(vehiculoId, correoTurista, fechaInicio, fechaFin, pasajeros, trayectos, null, null, null, null);
    }
}
