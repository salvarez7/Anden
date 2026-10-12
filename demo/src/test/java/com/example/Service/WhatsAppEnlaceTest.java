package com.example.Service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhatsAppEnlaceTest {

    @Test
    void armaElEnlaceConElIndicativoDeColombia() {
        String enlace = WhatsAppEnlace.crear("300 123 4567", "Hola");
        assertEquals("https://wa.me/573001234567?text=Hola", enlace);
    }

    @Test
    void elMensajeVaCodificadoSinMasParaLosEspacios() {
        String enlace = WhatsAppEnlace.crear("3001234567", "Hola, ¿qué tal?");
        assertTrue(enlace.contains("Hola%2C%20%C2%BFqu%C3%A9%20tal%3F"));
    }

    @Test
    void telefonoInvalidoNoGeneraEnlace() {
        assertNull(WhatsAppEnlace.crear("12345", "Hola"));
        assertNull(WhatsAppEnlace.crear(null, "Hola"));
    }

    @Test
    void elMensajeLlevaLosDatosYLasFechasDeLaReserva() {
        String m = WhatsAppEnlace.mensajeReserva("Ana", "Renault Duster",
                LocalDate.of(2026, 8, 14), LocalDate.of(2026, 8, 19));
        assertTrue(m.contains("Ana"));
        assertTrue(m.contains("Renault Duster"));
        assertTrue(m.contains("2026-08-14"));
        assertTrue(m.contains("2026-08-19"));
    }
}
