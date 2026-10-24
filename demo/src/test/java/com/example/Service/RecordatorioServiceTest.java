package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.PeriodoOcupado;
import com.example.Model.Usuario;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.VehiculoRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Sprint 3 (Sofía): recordatorio 3 días antes de la reserva, al conductor y al turista
class RecordatorioServiceTest {

    private final VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
    private final ConductorRepository conductorRepository = mock(ConductorRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final RecordatorioService servicio =
            new RecordatorioService(vehiculoRepository, conductorRepository, usuarioRepository);

    // Ejemplo de Sofía: reserva para el 21 de julio -> el aviso sale el 18
    private final LocalDate hoy = LocalDate.of(2027, 7, 18);
    private final LocalDate diaReserva = LocalDate.of(2027, 7, 21);

    private Vehiculo vehiculoConReserva(LocalDate inicio, String correoTurista) {
        Vehiculo v = new Vehiculo("juan@x.com", "Renault Duster", "estandar", 5);
        v.getPeriodosOcupados().add(new PeriodoOcupado(inicio, inicio.plusDays(2), correoTurista));
        return v;
    }

    private void conductor(String telefono) {
        Conductor c = new Conductor();
        c.setCorreo("juan@x.com");
        c.setNombre("Juan");
        c.setTelefono(telefono);
        when(conductorRepository.findByCorreo("juan@x.com")).thenReturn(Optional.of(c));
    }

    private void turista(String telefono) {
        when(usuarioRepository.findByCorreo("ana@x.com"))
                .thenReturn(Optional.of(new Usuario("Ana", "ana@x.com", telefono, "x", "TURISTA")));
    }

    @Test
    void elDia18AvisaLaReservaDel21AlConductorYAlTurista() {
        conductor("3001234567");
        turista("3115556677");
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculoConReserva(diaReserva, "ana@x.com")));

        List<Recordatorio> r = servicio.recordatoriosPara(hoy);

        assertEquals(2, r.size());
        assertEquals("CONDUCTOR", r.get(0).paraQuien());
        assertEquals("juan@x.com", r.get(0).correo());
        assertEquals("TURISTA", r.get(1).paraQuien());
        assertEquals("ana@x.com", r.get(1).correo());
    }

    @Test
    void noAvisaReservasDeOtrosDias() {
        conductor("3001234567");
        turista("3115556677");
        when(vehiculoRepository.findAll()).thenReturn(List.of(
                vehiculoConReserva(diaReserva.minusDays(1), "ana@x.com"),   // faltan 2 días
                vehiculoConReserva(diaReserva.plusDays(1), "ana@x.com")));  // faltan 4 días

        assertTrue(servicio.recordatoriosPara(hoy).isEmpty());
    }

    @Test
    void cadaUnoRecibeSuMensajeYSuEnlaceDeWhatsApp() {
        conductor("300 123 4567");
        turista("311 555 6677");
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculoConReserva(diaReserva, "ana@x.com")));

        List<Recordatorio> r = servicio.recordatoriosPara(hoy);

        assertEquals("Hola Juan, te recordamos que en 3 días tienes un servicio con Andén en el vehículo "
                + "Renault Duster, del 2027-07-21 al 2027-07-23.", r.get(0).mensaje());
        assertTrue(r.get(0).enlaceWhatsApp().startsWith("https://wa.me/573001234567?text=Hola%20Juan"));

        assertEquals("Hola Ana, te recordamos que en 3 días es tu reserva en Andén con Juan "
                + "(vehículo Renault Duster), del 2027-07-21 al 2027-07-23.", r.get(1).mensaje());
        assertTrue(r.get(1).enlaceWhatsApp().startsWith("https://wa.me/573115556677?text=Hola%20Ana"));
    }

    @Test
    void reservaSinTuristaSoloAvisaAlConductor() {
        conductor("3001234567");
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculoConReserva(diaReserva, null)));

        List<Recordatorio> r = servicio.recordatoriosPara(hoy);

        assertEquals(1, r.size());
        assertEquals("CONDUCTOR", r.get(0).paraQuien());
    }

    @Test
    void siNoEncuentraAlConductorIgualAvisaAlTurista() {
        when(conductorRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        turista("3115556677");
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculoConReserva(diaReserva, "ana@x.com")));

        List<Recordatorio> r = servicio.recordatoriosPara(hoy);

        assertEquals(1, r.size());
        assertEquals("TURISTA", r.get(0).paraQuien());
        assertTrue(r.get(0).mensaje().contains("es tu reserva en Andén (vehículo Renault Duster)"));
    }

    @Test
    void telefonoInvalidoDejaElRecordatorioSinEnlace() {
        conductor("12345");
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculoConReserva(diaReserva, null)));

        assertNull(servicio.recordatoriosPara(hoy).get(0).enlaceWhatsApp());
    }

    @Test
    void laTareaDiariaNoFallaSinDatos() {
        when(vehiculoRepository.findAll()).thenReturn(List.of());
        servicio.enviarRecordatoriosDeHoy();
    }

    // ---- Recordatorio de cancelación (2 semanas antes) ----

    @Test
    void dosSemanasAntesAvisaLaCancelacionAlConductorYAlTurista() {
        conductor("3001234567");
        turista("3115556677");
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculoConReserva(diaReserva, "ana@x.com")));

        List<Recordatorio> r = servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CANCELACION);

        assertEquals(2, r.size());
        assertEquals("CONDUCTOR", r.get(0).paraQuien());
        assertEquals(TipoNotificacion.CANCELACION, r.get(0).tipo());
        assertEquals("TURISTA", r.get(1).paraQuien());
        assertEquals(TipoNotificacion.CANCELACION, r.get(1).tipo());
    }

    @Test
    void laCancelacionNoAvisaReservasDeOtrosDias() {
        conductor("3001234567");
        turista("3115556677");
        when(vehiculoRepository.findAll()).thenReturn(List.of(
                vehiculoConReserva(diaReserva.minusDays(1), "ana@x.com"),   // faltan 13 días
                vehiculoConReserva(diaReserva.plusDays(1), "ana@x.com")));  // faltan 15 días

        assertTrue(servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CANCELACION).isEmpty());
    }

    @Test
    void cadaTipoAvisaSoloEnSuPropioDia() {
        conductor("3001234567");
        turista("3115556677");
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculoConReserva(diaReserva, "ana@x.com")));

        assertEquals(2, servicio.recordatoriosPara(diaReserva.minusDays(3), TipoNotificacion.CONFIRMACION).size());
        assertTrue(servicio.recordatoriosPara(diaReserva.minusDays(3), TipoNotificacion.CANCELACION).isEmpty());
        assertEquals(2, servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CANCELACION).size());
        assertTrue(servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CONFIRMACION).isEmpty());
    }

    @Test
    void elMensajeDeCancelacionLlevaLosDatosYElEnlaceDeWhatsApp() {
        conductor("300 123 4567");
        turista("311 555 6677");
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculoConReserva(diaReserva, "ana@x.com")));

        List<Recordatorio> r = servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CANCELACION);

        assertEquals("Hola Juan, faltan 2 semanas para tu servicio con Andén en el vehículo Renault Duster, "
                        + "del 2027-07-21 al 2027-07-23. Si no vas a poder cumplirlo, este es el momento de cancelar.",
                r.get(0).mensaje());
        assertTrue(r.get(0).enlaceWhatsApp().startsWith("https://wa.me/573001234567?text=Hola%20Juan"));

        assertEquals("Hola Ana, faltan 2 semanas para tu reserva en Andén con Juan (vehículo Renault Duster), "
                        + "del 2027-07-21 al 2027-07-23. Si necesitas cancelar, este es el momento de hacerlo.",
                r.get(1).mensaje());
        assertTrue(r.get(1).enlaceWhatsApp().startsWith("https://wa.me/573115556677?text=Hola%20Ana"));
    }
}
