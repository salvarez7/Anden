package com.example.Service;

import com.example.Model.Gama;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValorFinalCalculadoraTest {

    @Test
    void cobraLasHorasYLosKilometrosReales() {
        // Económica: $10.000 la hora y $1.250 el km -> 8 h y 100 km
        assertEquals(205_000L, ValorFinalCalculadora.calcular(Gama.ECONOMICA, 1, 8, 3, 100));
    }

    @Test
    void desdeElQuintoPasajeroCobraRecargoPorCadaDia() {
        // Estándar: 10 h * 12.000 + 150 km * 1.450 + (6 - 4) pasajeros * 5.000 * 2 días
        assertEquals(357_500L, ValorFinalCalculadora.calcular(Gama.ESTANDAR, 2, 10, 6, 150));
    }

    @Test
    void conCuatroPasajerosNoHayRecargo() {
        // Premium: 5 h * 16.000 + 10 km * 1.900
        assertEquals(99_000L, ValorFinalCalculadora.calcular(Gama.PREMIUM, 1, 5, 4, 10));
    }

    @Test
    void elValorSeRedondeaALaCentena() {
        // Estándar: 1 h * 12.000 + 1 km * 1.450 = 13.450 -> 13.500
        assertEquals(13_500L, ValorFinalCalculadora.calcular(Gama.ESTANDAR, 1, 1, 1, 1));
    }

    @Test
    void sinKilometrosSoloSeCobranLasHoras() {
        assertEquals(36_000L, ValorFinalCalculadora.calcular(Gama.ESTANDAR, 1, 3, 2, 0));
    }

    @Test
    void validaLosKilometros() {
        assertNotNull(ValorFinalCalculadora.validarKilometros(null));
        assertTrue(ValorFinalCalculadora.validarKilometros(-1).contains("0 o más"));
        assertNull(ValorFinalCalculadora.validarKilometros(0));
        assertNull(ValorFinalCalculadora.validarKilometros(5000));
        assertTrue(ValorFinalCalculadora.validarKilometros(5001).contains("5000"));
    }

    @Test
    void validaLasHorasSegunLosDiasDelServicio() {
        assertNotNull(ValorFinalCalculadora.validarHoras(null, 2));
        assertTrue(ValorFinalCalculadora.validarHoras(0, 2).contains("mínimo 1"));
        assertNull(ValorFinalCalculadora.validarHoras(1, 2));
        assertNull(ValorFinalCalculadora.validarHoras(48, 2));
        assertTrue(ValorFinalCalculadora.validarHoras(49, 2).contains("48"));
    }
}
