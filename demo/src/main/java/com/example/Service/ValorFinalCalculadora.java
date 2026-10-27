package com.example.Service;

import com.example.Model.Gama;


public final class ValorFinalCalculadora {

    static final int MAXIMO_KILOMETROS = 5000;
    static final int MAXIMO_HORAS_POR_DIA = 24;

    private ValorFinalCalculadora() {
    }

    public static long calcular(Gama gama, int dias, int horasReales, int pasajeros, int kilometrosReales) {
        long porHoras = (long) horasReales * gama.getTarifaHora();
        long porKilometros = (long) kilometrosReales * gama.getTarifaKm();
        long recargoPasajeros = (long) Math.max(0, pasajeros - CalculadoraTarifa.PASAJEROS_INCLUIDOS)
                * CalculadoraTarifa.RECARGO_PASAJERO_DIA * dias;
        return CalculadoraTarifa.redondearCien(porHoras + porKilometros + recargoPasajeros);
    }
    
    public static String validarKilometros(Integer kilometros) {
        if (kilometros == null || kilometros < 0) {
            return "Indica los kilómetros recorridos (0 o más).";
        }
        if (kilometros > MAXIMO_KILOMETROS) {
            return "Los kilómetros no pueden pasar de " + MAXIMO_KILOMETROS + ".";
        }
        return null;
    }

    public static String validarHoras(Integer horas, int dias) {
        if (horas == null || horas < 1) {
            return "Indica las horas trabajadas (mínimo 1).";
        }
        int maximo = dias * MAXIMO_HORAS_POR_DIA;
        if (horas > maximo) {
            return "Las horas no pueden pasar de " + maximo + " para " + dias + " día(s).";
        }
        return null;
    }
}
