package com.example.Service;

import com.example.Model.Reserva;

public record ReservaConContacto(Reserva reserva, String nombreConductor, String modeloVehiculo,
                                 String enlaceWhatsApp) {
}
