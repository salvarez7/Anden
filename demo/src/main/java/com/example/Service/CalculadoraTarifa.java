package com.example.Service;

import com.example.Model.Gama;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


// Se cobra por las horas del conductor y por los kilómetros (gasolina, desgaste y recorrido), con recargo en hora pico y de noche
public final class CalculadoraTarifa {

    static final int KM_POR_TRAYECTO = 12;           // un trayecto promedio dentro de Medellín
    static final int KM_POR_TRAYECTO_FUERA = 80;     // un viaje a otro municipio (ej. Medellín - Guatapé)
    static final int PASAJEROS_INCLUIDOS = 4;        // desde el 5.º pasajero se cobra recargo
    static final int RECARGO_PASAJERO_DIA = 5000;
    static final double RECARGO_PICO = 0.20;         // 6 a 9 a. m. y 5 a 8 p. m.
    static final double RECARGO_NOCHE = 0.30;        // 8 p. m. a 6 a. m.
    static final double MARGEN = 0.15;               // el rango que se muestra: ±15 %
    static final int MAXIMO_TRAYECTOS = 50;
    static final int MAXIMO_TRAYECTOS_FUERA = 20;
    static final int MAXIMO_HORAS_POR_DIA = 12;

    public static final String AVISO = "Este valor es solo una estimación. El precio final se define al concluir el viaje. "
            + "Si los trayectos resultan más largos de lo indicado, el ajuste se acuerda con el conductor.";

    private CalculadoraTarifa() {
    }

    public static TarifaEstimada calcular(Gama gama, int dias, int horasPorDia, int pasajeros,
                                          int trayectos, int trayectosFuera, int horaInicio) {
        int horas = dias * horasPorDia;
        int kilometros = trayectos * KM_POR_TRAYECTO + trayectosFuera * KM_POR_TRAYECTO_FUERA;
        long porHoras = (long) horas * gama.getTarifaHora();
        long porKilometros = (long) kilometros * gama.getTarifaKm();
        long recargoPasajeros = (long) Math.max(0, pasajeros - PASAJEROS_INCLUIDOS) * RECARGO_PASAJERO_DIA * dias;


        int horasPico = 0;
        int horasNoche = 0;
        for (int i = 0; i < horasPorDia; i++) {
            int hora = (horaInicio + i) % 24;
            if (esNoche(hora)) {
                horasNoche++;
            } else if (esHoraPico(hora)) {
                horasPico++;
            }
        }
        double recargo = (horasPico * RECARGO_PICO + horasNoche * RECARGO_NOCHE) / horasPorDia;
        long recargoHorario = redondearCien((porHoras + porKilometros) * recargo);

        long total = porHoras + porKilometros + recargoPasajeros + recargoHorario;
        return new TarifaEstimada(gama, dias, horasPorDia, pasajeros, trayectos, trayectosFuera, horaInicio,
                horas, kilometros, horasPico, horasNoche,
                porHoras, porKilometros, recargoPasajeros, recargoHorario, total,
                redondearMil(total * (1 - MARGEN)), redondearMil(total * (1 + MARGEN)));
    }


    public static Optional<TarifaEstimada> calcular(String codigoGama, LocalDate inicio, LocalDate fin,
                                                    Integer horasPorDia, Integer pasajeros,
                                                    Integer trayectos, Integer trayectosFuera, Integer horaInicio) {
        Gama gama = Gama.desde(codigoGama);
        if (gama == null || inicio == null || fin == null || fin.isBefore(inicio)
                || horasPorDia == null || horasPorDia < 1
                || pasajeros == null || pasajeros < 1 || trayectos == null || trayectos < 0
                || trayectosFuera == null || trayectosFuera < 0
                || horaInicio == null || horaInicio < 0 || horaInicio > 23) {
            return Optional.empty();
        }
        return Optional.of(calcular(gama, dias(inicio, fin), horasPorDia, pasajeros, trayectos, trayectosFuera,
                horaInicio));
    }

    static boolean esNoche(int hora) {
        return hora >= 20 || hora < 6;
    }

    static boolean esHoraPico(int hora) {
        return (hora >= 6 && hora < 9) || (hora >= 17 && hora < 20);
    }

    // 7 -> "7:00 a. m. (hora pico)", para la lista del formulario
    public static String etiquetaHora(int hora) {
        int hora12 = hora % 12 == 0 ? 12 : hora % 12;
        String texto = hora12 + ":00 " + (hora < 12 ? "a. m." : "p. m.");
        if (esNoche(hora)) {
            return texto + " (noche)";
        }
        return esHoraPico(hora) ? texto + " (hora pico)" : texto;
    }


    public static int dias(LocalDate inicio, LocalDate fin) {
        return (int) ChronoUnit.DAYS.between(inicio, fin) + 1;
    }


    public static String validarNumeroTrayectos(Integer trayectos, boolean obligatorio) {
        if (trayectos == null) {
            return obligatorio ? "Indica cuántos trayectos vas a hacer dentro de Medellín (puede ser 0)." : null;
        }
        if (trayectos < 0 || trayectos > MAXIMO_TRAYECTOS) {
            return "El número de trayectos debe estar entre 0 y " + MAXIMO_TRAYECTOS + ".";
        }
        return null;
    }

    public static String validarTrayectosFuera(Integer trayectosFuera, boolean obligatorio) {
        if (trayectosFuera == null) {
            return obligatorio ? "Indica cuántos viajes vas a hacer fuera de Medellín (puede ser 0)." : null;
        }
        if (trayectosFuera < 0 || trayectosFuera > MAXIMO_TRAYECTOS_FUERA) {
            return "Los viajes fuera de Medellín deben estar entre 0 y " + MAXIMO_TRAYECTOS_FUERA + ".";
        }
        return null;
    }

    public static String validarHorasPorDia(Integer horas, boolean obligatorio) {
        if (horas == null) {
            return obligatorio ? "Indica cuántas horas al día necesitas el carro." : null;
        }
        if (horas < 1 || horas > MAXIMO_HORAS_POR_DIA) {
            return "Las horas al día deben estar entre 1 y " + MAXIMO_HORAS_POR_DIA + ".";
        }
        return null;
    }

    public static String validarHoraInicio(Integer hora, boolean obligatorio) {
        if (hora == null) {
            return obligatorio ? "Indica a qué hora empieza el servicio cada día." : null;
        }
        if (hora < 0 || hora > 23) {
            return "La hora de inicio no es válida.";
        }
        return null;
    }

    static long redondearMil(double valor) {
        return Math.round(valor / 1000) * 1000;
    }

    static long redondearCien(double valor) {
        return Math.round(valor / 100) * 100;
    }
}
