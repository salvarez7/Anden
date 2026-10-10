package com.example.Service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroServiceTest {

    
    private final RegistroService servicio = new RegistroService(null, null, null);

    private DatosRegistro conductor(int edad, LocalDate fechaLicencia) {
        // SOAT y tecnomecánica vigentes (vencen dentro de un año)
        return conductor(edad, fechaLicencia,
                LocalDate.now().plusYears(1), LocalDate.now().plusYears(1));
    }

    private DatosRegistro conductor(int edad, LocalDate fechaLicencia,
                                    LocalDate soat, LocalDate tecno) {
        return new DatosRegistro("Juan Pérez", "juan@correo.com", "3001234567",
                "123456", "CONDUCTOR", edad, "LIC123", fechaLicencia, "ABC123",
                soat, tecno);
    }

    @Test
    void conductorQueCumpleTodoEsValido() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(30, LocalDate.now().minusYears(5)));
        assertTrue(r.valido());
    }

    @Test
    void menorDeEdadNoEsValido() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(17, LocalDate.now().minusYears(5)));
        assertFalse(r.valido());
        assertTrue(r.errores().get(0).contains("mayor de edad"));
    }

    @Test
    void licenciaConUnAnoNoEsValida() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(30, LocalDate.now().minusYears(1)));
        assertFalse(r.valido());
        assertTrue(r.errores().get(0).contains("2 años"));
    }

    @Test
    void licenciaConDosAnosExactosEsValida() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(30, LocalDate.now().minusYears(2)));
        assertTrue(r.valido());
    }

    @Test
    void sinFechaDeLicenciaNoEsValido() {
        ResultadoValidacion r = servicio.validarRequisitos(conductor(30, null));
        assertFalse(r.valido());
    }

    @Test
    void soatVencidoNoEsValido() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(30, LocalDate.now().minusYears(5),
                        LocalDate.now().minusDays(1), LocalDate.now().plusYears(1)));
        assertFalse(r.valido());
        assertTrue(r.errores().get(0).contains("SOAT"));
    }

    @Test
    void tecnomecanicaVencidaNoEsValida() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(30, LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(1), LocalDate.now().minusDays(1)));
        assertFalse(r.valido());
        assertTrue(r.errores().get(0).contains("tecnomecánica"));
    }

    @Test
    void sinFechasDeSoatYTecnomecanicaNoEsValido() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(30, LocalDate.now().minusYears(5), null, null));
        assertFalse(r.valido());
        assertEquals(2, r.errores().size());
    }

    @Test
    void soatQueVenceHoySigueVigente() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(30, LocalDate.now().minusYears(5),
                        LocalDate.now(), LocalDate.now()));
        assertTrue(r.valido());
    }

    @Test
    void siFallanDosRequisitosSeMuestranLosDosMensajes() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(16, LocalDate.now().minusMonths(6)));
        assertFalse(r.valido());
        assertEquals(2, r.errores().size());
    }
}
