package com.example.Service;

import com.example.Model.PeriodoOcupado;
import com.example.Model.Vehiculo;

// Por cada reserva sale un recordatorio para el conductor y otro para el turista
public record Recordatorio(String paraQuien, String nombre, String correo,
                           Vehiculo vehiculo, PeriodoOcupado periodo,
                           String mensaje, String enlaceWhatsApp) {
}
