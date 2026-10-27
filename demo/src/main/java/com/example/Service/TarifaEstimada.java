package com.example.Service;

import com.example.Model.Gama;

import java.text.NumberFormat;
import java.util.Locale;

// resultado del cálculo
public record TarifaEstimada(Gama gama, int dias, int horasPorDia, int pasajeros,
                             int trayectos, int trayectosFuera, int horaInicio, int horas, int kilometros,
                             int horasPico, int horasNoche,
                             long porHoras, long porKilometros, long recargoPasajeros, long recargoHorario,
                             long total, long minimo, long maximo) {


    public static String pesos(long valor) {
        NumberFormat formato = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));
        return "$" + formato.format(valor);
    }
}
