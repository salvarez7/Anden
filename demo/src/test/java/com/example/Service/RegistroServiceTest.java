package com.example.Service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroServiceTest {

    private final RegistroService servicio = new RegistroService(null, null, null);

    private DatosRegistro conductor(int edad, LocalDate fechaLicencia) {
        return new DatosRegistro("Juan Pérez", "juan@correo.com", "3001234567",
                "123456", "CONDUCTOR", edad, "LIC123", fechaLicencia, "ABC123",
                "Renault", "Duster", 4,
                LocalDate.now().plusMonths(6), LocalDate.now().plusMonths(6));
    }

    private DatosRegistro turista(String correo, String telefono) {
        return new DatosRegistro("Ana Gómez", correo, telefono, "123456", "TURISTA",
                0, null, null, null, null, null, 0, null, null);
    }

    private DatosRegistro conductorConPapeles(LocalDate soat, LocalDate tecnomecanica) {
        return new DatosRegistro("Juan Pérez", "juan@correo.com", "3001234567",
                "123456", "CONDUCTOR", 30, "LIC123", LocalDate.now().minusYears(5), "ABC123",
                "Renault", "Duster", 4, soat, tecnomecanica);
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
    void siFallanDosRequisitosSeMuestranLosDosMensajes() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductor(16, LocalDate.now().minusMonths(6)));
        assertFalse(r.valido());
        assertEquals(2, r.errores().size());
    }

    @Test
    void turistaConDatosCorrectosEsValido() {
        assertTrue(servicio.validarDatosBasicos(turista("ana@correo.com", "3001234567")).valido());
    }

    @Test
    void telefonoConEspaciosYPrefijoEsValido() {
        assertTrue(servicio.validarDatosBasicos(turista("ana@correo.com", "+57 300 123 4567")).valido());
        assertEquals("3001234567", RegistroService.normalizarTelefono("+57 300 123-4567"));
    }

    @Test
    void correoSinArrobaNoEsValido() {
        ResultadoValidacion r = servicio.validarDatosBasicos(turista("anacorreo.com", "3001234567"));
        assertFalse(r.valido());
        assertTrue(r.errores().get(0).contains("correo"));
    }

    @Test
    void telefonoCortoNoEsValido() {
        ResultadoValidacion r = servicio.validarDatosBasicos(turista("ana@correo.com", "30012"));
        assertFalse(r.valido());
        assertTrue(r.errores().get(0).contains("teléfono"));
    }

    @Test
    void telefonoConLetrasNoEsValido() {
        assertFalse(servicio.validarDatosBasicos(turista("ana@correo.com", "300abc4567")).valido());
    }

    @Test
    void noSePuedeRegistrarComoAdministrador() {
        DatosRegistro admin = new DatosRegistro("X", "x@correo.com", "3001234567", "123456",
                "ADMIN", 0, null, null, null, null, null, 0, null, null);
        assertFalse(servicio.validarDatosBasicos(admin).valido());
    }


    @Test
    void soatVencidoNoEsValido() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductorConPapeles(LocalDate.now().minusDays(1), LocalDate.now().plusMonths(3)));
        assertFalse(r.valido());
        assertTrue(r.errores().get(0).contains("SOAT"));
    }

    @Test
    void tecnomecanicaVencidaNoEsValida() {
        ResultadoValidacion r = servicio.validarRequisitos(
                conductorConPapeles(LocalDate.now().plusMonths(3), LocalDate.now().minusMonths(1)));
        assertFalse(r.valido());
        assertTrue(r.errores().get(0).contains("tecnomecánica"));
    }
}
