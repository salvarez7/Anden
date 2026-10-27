package com.example.Service;

import com.example.Model.Gama;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraTarifaTest {

    @Test
    void cuentaLosDiasIncluyendoElPrimeroYElUltimo() {
        assertEquals(4, CalculadoraTarifa.dias(LocalDate.of(2027, 7, 21), LocalDate.of(2027, 7, 24)));
        assertEquals(1, CalculadoraTarifa.dias(LocalDate.of(2027, 7, 21), LocalDate.of(2027, 7, 21)));
    }

    @Test
    void medellinGuatapeCuestaParecidoAInDriver() {
        // 2 horas de conductor + 80 km en estándar, a las 10 a. m. InDriver cobra entre $120.000 y $150.000
        TarifaEstimada t = CalculadoraTarifa.calcular(Gama.ESTANDAR, 1, 2, 2, 0, 1, 10);

        assertEquals(80, t.kilometros());
        assertEquals(24_000, t.porHoras());           // 2 h × 12.000
        assertEquals(116_000, t.porKilometros());     // 80 km × 1.450
        assertEquals(0, t.recargoHorario());
        assertEquals(140_000, t.total());
    }

    @Test
    void enHoraPicoYDeNocheCuestaMas() {
        TarifaEstimada pico = CalculadoraTarifa.calcular(Gama.ESTANDAR, 1, 2, 2, 0, 1, 7);
        assertEquals(2, pico.horasPico());
        assertEquals(28_000, pico.recargoHorario());  // +20 %
        assertEquals(168_000, pico.total());

        TarifaEstimada noche = CalculadoraTarifa.calcular(Gama.ESTANDAR, 1, 2, 2, 0, 1, 21);
        assertEquals(2, noche.horasNoche());
        assertEquals(42_000, noche.recargoHorario()); // +30 %
        assertEquals(182_000, noche.total());
    }

    @Test
    void lasHorasPasanDeLaMedianoche() {
        // De 10 p. m. a 8 a. m.: 8 horas de noche (22 a 5) y 2 de hora pico (6 y 7)
        TarifaEstimada t = CalculadoraTarifa.calcular(Gama.ESTANDAR, 1, 10, 2, 0, 0, 22);
        assertEquals(8, t.horasNoche());
        assertEquals(2, t.horasPico());
    }

    @Test
    void unTrayectoEnLaCiudadCuestaParecidoAUber() {
        // 1 hora de conductor + 12 km en estándar
        assertEquals(29_400, CalculadoraTarifa.calcular(Gama.ESTANDAR, 1, 1, 2, 1, 0, 10).total());
    }

    @Test
    void estandarCuatroDiasConUnViajeFuera() {
        // Desde las 7 a. m., 6 horas: 7 y 8 son hora pico
        TarifaEstimada t = CalculadoraTarifa.calcular(Gama.ESTANDAR, 4, 6, 3, 6, 1, 7);

        assertEquals(24, t.horas());                  // 4 días × 6 horas
        assertEquals(152, t.kilometros());            // 6 × 12 km + 1 × 80 km
        assertEquals(288_000, t.porHoras());          // 24 × 12.000
        assertEquals(220_400, t.porKilometros());     // 152 × 1.450
        assertEquals(0, t.recargoPasajeros());        // hasta 4 pasajeros no hay recargo
        assertEquals(33_900, t.recargoHorario());     // 508.400 × 20 % × 2 de 6 horas, redondeado a cientos
        assertEquals(542_300, t.total());
        assertEquals(461_000, t.minimo());            // -15 %, redondeado a miles
        assertEquals(624_000, t.maximo());            // +15 %, redondeado a miles
    }

    @Test
    void desdeElQuintoPasajeroSeCobraRecargoPorDia() {
        TarifaEstimada t = CalculadoraTarifa.calcular(Gama.ECONOMICA, 2, 8, 6, 0, 0, 9);

        assertEquals(160_000, t.porHoras());          // 16 h × 10.000
        assertEquals(20_000, t.recargoPasajeros());   // 2 pasajeros extra × 5.000 × 2 días
        assertEquals(180_000, t.total());
    }

    @Test
    void cadaGamaTieneSuTarifa() {
        long economica = CalculadoraTarifa.calcular(Gama.ECONOMICA, 3, 5, 2, 3, 0, 9).total();
        long estandar = CalculadoraTarifa.calcular(Gama.ESTANDAR, 3, 5, 2, 3, 0, 9).total();
        long premium = CalculadoraTarifa.calcular(Gama.PREMIUM, 3, 5, 2, 3, 0, 9).total();

        assertEquals(195_000, economica);             // 15 h × 10.000 + 36 km × 1.250
        assertEquals(232_200, estandar);              // 15 h × 12.000 + 36 km × 1.450
        assertEquals(308_400, premium);               // 15 h × 16.000 + 36 km × 1.900
        assertTrue(economica < estandar && estandar < premium);
    }

    @Test
    void conDatosIncompletosNoHayEstimado() {
        LocalDate hoy = LocalDate.of(2027, 7, 21);
        assertTrue(CalculadoraTarifa.calcular("no-existe", hoy, hoy, 1, 2, 1, 0, 10).isEmpty());
        assertTrue(CalculadoraTarifa.calcular("estandar", hoy, hoy.minusDays(1), 1, 2, 1, 0, 10).isEmpty());
        assertTrue(CalculadoraTarifa.calcular("estandar", hoy, hoy, null, 2, 1, 0, 10).isEmpty());
        assertTrue(CalculadoraTarifa.calcular("estandar", hoy, hoy, 1, null, 1, 0, 10).isEmpty());
        assertTrue(CalculadoraTarifa.calcular("estandar", hoy, hoy, 1, 2, null, 0, 10).isEmpty());
        assertTrue(CalculadoraTarifa.calcular("estandar", hoy, hoy, 1, 2, 1, null, 10).isEmpty());
        assertTrue(CalculadoraTarifa.calcular("estandar", hoy, hoy, 1, 2, 1, 0, null).isEmpty());
        assertTrue(CalculadoraTarifa.calcular("estandar", hoy, hoy, 1, 2, 1, 0, 24).isEmpty());
        assertEquals(29_400, CalculadoraTarifa.calcular("estandar", hoy, hoy, 1, 2, 1, 0, 10).get().total());
    }

    @Test
    void validaElNumeroDeTrayectos() {
        assertNotNull(CalculadoraTarifa.validarNumeroTrayectos(null, true));
        assertNull(CalculadoraTarifa.validarNumeroTrayectos(null, false));
        assertNull(CalculadoraTarifa.validarNumeroTrayectos(0, true));
        assertNotNull(CalculadoraTarifa.validarNumeroTrayectos(-1, true));
        assertNotNull(CalculadoraTarifa.validarNumeroTrayectos(51, true));
        assertNotNull(CalculadoraTarifa.validarTrayectosFuera(null, true));
        assertNull(CalculadoraTarifa.validarTrayectosFuera(0, true));
        assertNotNull(CalculadoraTarifa.validarTrayectosFuera(21, true));
    }

    @Test
    void validaLasHorasAlDia() {
        assertNotNull(CalculadoraTarifa.validarHorasPorDia(null, true));
        assertNull(CalculadoraTarifa.validarHorasPorDia(null, false));
        assertNull(CalculadoraTarifa.validarHorasPorDia(8, true));
        assertNotNull(CalculadoraTarifa.validarHorasPorDia(0, true));
        assertNotNull(CalculadoraTarifa.validarHorasPorDia(13, true));
        assertNotNull(CalculadoraTarifa.validarHoraInicio(null, true));
        assertNull(CalculadoraTarifa.validarHoraInicio(0, true));
        assertNotNull(CalculadoraTarifa.validarHoraInicio(24, true));
    }

    @Test
    void escribeLaHoraConSuRecargo() {
        assertEquals("7:00 a. m. (hora pico)", CalculadoraTarifa.etiquetaHora(7));
        assertEquals("12:00 a. m. (noche)", CalculadoraTarifa.etiquetaHora(0));
        assertEquals("1:00 p. m.", CalculadoraTarifa.etiquetaHora(13));
        assertEquals("6:00 p. m. (hora pico)", CalculadoraTarifa.etiquetaHora(18));
        assertEquals("8:00 p. m. (noche)", CalculadoraTarifa.etiquetaHora(20));
    }

    @Test
    void escribeLosValoresEnPesos() {
        assertEquals("$542.300", TarifaEstimada.pesos(542_300));
        assertEquals("$1.070.000", TarifaEstimada.pesos(1_070_000));
        assertEquals("$25.000", TarifaEstimada.pesos(25_000));
    }
}
