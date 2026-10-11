package com.example.Service;

import java.time.LocalDate;

public record DatosRegistro(
        String nombre,
        String correo,
        String telefono,
        String password,
        String tipoUsuario, // "CONDUCTOR" o "TURISTA"
        int edad,
        String numeroLicencia,
        LocalDate fechaExpedicionLicencia,
        String placaVehiculo,
        String marcaVehiculo,
        String modeloVehiculo,
        int asientosVehiculo,
        LocalDate fechaVencimientoSoat,
        LocalDate fechaVencimientoTecnomecanica) {
}
