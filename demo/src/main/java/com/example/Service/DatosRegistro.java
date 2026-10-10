package com.example.Service;

import java.time.LocalDate;

// Paquete de datos que llega desde el formulario de registro (Sofía).
// Para un turista basta con llenar nombre, correo, telefono, password y tipoUsuario;
// lo demás (edad, licencia, placa) puede ir en 0 / null.
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
        LocalDate fechaVencimientoSoat,
        LocalDate fechaVencimientoTecnomecanica) {
}
