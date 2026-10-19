package com.example.Service;

import com.example.Model.PeriodoOcupado;
import com.example.Model.Vehiculo;

// Por cada reserva salen dos avisos de cada tipo: uno para el conductor y otro para el turista
public record Recordatorio(String paraQuien, String nombre, String correo,
                           Vehiculo vehiculo, PeriodoOcupado periodo,
                           String mensaje, String enlaceWhatsApp,
                           TipoNotificacion tipo) {
}