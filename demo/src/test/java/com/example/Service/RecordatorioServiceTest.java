package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.PeriodoOcupado;
import com.example.Model.Reserva;
import com.example.Model.Usuario;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.ReservaRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class RecordatorioServiceTest {

    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private final VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
    private final ConductorRepository conductorRepository = mock(ConductorRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final RecordatorioService servicio =
            new RecordatorioService(reservaRepository, vehiculoRepository, conductorRepository, usuarioRepository);

    
    private final LocalDate hoy = LocalDate.of(2027, 7, 18);
    private final LocalDate diaReserva = LocalDate.of(2027, 7, 21);

    private final List<Reserva> reservas = new ArrayList<>();
    private int numero = 1000;

    @BeforeEach
    void simularLaBaseDeDatos() {
        Vehiculo duster = new Vehiculo("juan@x.com", "Renault Duster", "estandar", 5);
        duster.setId("v1");
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(duster));
       
        when(reservaRepository.findByEstadoAndFechaInicio(anyString(), any(LocalDate.class)))
                .thenAnswer(pedido -> reservas.stream()
                        .filter(r -> r.getEstado().equals(pedido.getArgument(0)))
                        .filter(r -> r.getFechaInicio().equals(pedido.getArgument(1)))
                        .toList());
    }

   
    private Reserva reserva(LocalDate inicio, String correoTurista) {
        Reserva r = new Reserva("AND-" + numero++, "v1", "juan@x.com", correoTurista,
                inicio, inicio.plusDays(2), 2, "Aeropuerto a hotel");
        reservas.add(r);
        return r;
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
        reserva(diaReserva, "ana@x.com");

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
        reserva(diaReserva.minusDays(1), "ana@x.com");   // faltan 2 días
        reserva(diaReserva.plusDays(1), "ana@x.com");    // faltan 4 días

        assertTrue(servicio.recordatoriosPara(hoy).isEmpty());
    }

    @Test
    void cadaUnoRecibeSuMensajeYSuEnlaceDeWhatsApp() {
        conductor("300 123 4567");
        turista("311 555 6677");
        reserva(diaReserva, "ana@x.com");

        List<Recordatorio> r = servicio.recordatoriosPara(hoy);

        assertEquals("Hola Juan, te recordamos que en 3 días tienes un servicio con Andén en el vehículo "
                + "Renault Duster, del 2027-07-21 al 2027-07-23. Código de reserva: AND-1000.", r.get(0).mensaje());
        assertTrue(r.get(0).enlaceWhatsApp().startsWith("https://wa.me/573001234567?text=Hola%20Juan"));

        assertEquals("Hola Ana, te recordamos que en 3 días es tu reserva en Andén con Juan "
                + "(vehículo Renault Duster), del 2027-07-21 al 2027-07-23. Código de reserva: AND-1000.", r.get(1).mensaje());
        assertTrue(r.get(1).enlaceWhatsApp().startsWith("https://wa.me/573115556677?text=Hola%20Ana"));
    }

    @Test
    void reservaSinTuristaSoloAvisaAlConductor() {
        conductor("3001234567");
        reserva(diaReserva, null);

        List<Recordatorio> r = servicio.recordatoriosPara(hoy);

        assertEquals(1, r.size());
        assertEquals("CONDUCTOR", r.get(0).paraQuien());
    }

    @Test
    void siNoEncuentraAlConductorIgualAvisaAlTurista() {
        when(conductorRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        turista("3115556677");
        reserva(diaReserva, "ana@x.com");

        List<Recordatorio> r = servicio.recordatoriosPara(hoy);

        assertEquals(1, r.size());
        assertEquals("TURISTA", r.get(0).paraQuien());
        assertTrue(r.get(0).mensaje().contains("es tu reserva en Andén (vehículo Renault Duster)"));
    }

    @Test
    void telefonoInvalidoDejaElRecordatorioSinEnlace() {
        conductor("12345");
        reserva(diaReserva, null);

        assertNull(servicio.recordatoriosPara(hoy).get(0).enlaceWhatsApp());
    }

    @Test
    void laTareaDiariaNoFallaSinDatos() {
        servicio.enviarRecordatoriosDeHoy();
    }

    // ---- Recordatorio de cancelación (2 semanas antes) ----

    @Test
    void dosSemanasAntesAvisaLaCancelacionAlConductorYAlTurista() {
        conductor("3001234567");
        turista("3115556677");
        reserva(diaReserva, "ana@x.com");

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
        reserva(diaReserva.minusDays(1), "ana@x.com");   // faltan 13 días
        reserva(diaReserva.plusDays(1), "ana@x.com");    // faltan 15 días

        assertTrue(servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CANCELACION).isEmpty());
    }

    @Test
    void cadaTipoAvisaSoloEnSuPropioDia() {
        conductor("3001234567");
        turista("3115556677");
        reserva(diaReserva, "ana@x.com");

        assertEquals(2, servicio.recordatoriosPara(diaReserva.minusDays(3), TipoNotificacion.CONFIRMACION).size());
        assertTrue(servicio.recordatoriosPara(diaReserva.minusDays(3), TipoNotificacion.CANCELACION).isEmpty());
        assertEquals(2, servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CANCELACION).size());
        assertTrue(servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CONFIRMACION).isEmpty());
    }

    @Test
    void elMensajeDeCancelacionLlevaLosDatosYElEnlaceDeWhatsApp() {
        conductor("300 123 4567");
        turista("311 555 6677");
        reserva(diaReserva, "ana@x.com");

        List<Recordatorio> r = servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CANCELACION);

        assertEquals("Hola Juan, faltan 2 semanas para tu servicio con Andén en el vehículo Renault Duster, "
                        + "del 2027-07-21 al 2027-07-23. Si no vas a poder cumplirlo, este es el momento de cancelar. Código de reserva: AND-1000.",
                r.get(0).mensaje());
        assertTrue(r.get(0).enlaceWhatsApp().startsWith("https://wa.me/573001234567?text=Hola%20Juan"));

        assertEquals("Hola Ana, faltan 2 semanas para tu reserva en Andén con Juan (vehículo Renault Duster), "
                        + "del 2027-07-21 al 2027-07-23. Si necesitas cancelar, este es el momento de hacerlo. Código de reserva: AND-1000.",
                r.get(1).mensaje());
        assertTrue(r.get(1).enlaceWhatsApp().startsWith("https://wa.me/573115556677?text=Hola%20Ana"));
    }

    

    @Test
    void unaReservaCanceladaNoRecibeAvisos() {
        conductor("3001234567");
        turista("3115556677");
        reserva(diaReserva, "ana@x.com").setEstado("CANCELADA");

        assertTrue(servicio.recordatoriosPara(hoy).isEmpty());
        assertTrue(servicio.recordatoriosPara(diaReserva.minusDays(14), TipoNotificacion.CANCELACION).isEmpty());
    }

    @Test
    void unaReservaQueNoEstaConfirmadaNoRecibeAvisos() {
        conductor("3001234567");
        turista("3115556677");
        reserva(diaReserva, "ana@x.com").setEstado("PENDIENTE");

        assertTrue(servicio.recordatoriosPara(hoy).isEmpty());
    }

    @Test
    void siHayUnaCanceladaYUnaConfirmadaSoloSeAvisaLaConfirmada() {
        conductor("3001234567");
        turista("3115556677");
        reserva(diaReserva, "ana@x.com").setEstado("CANCELADA");
        reserva(diaReserva, "ana@x.com");

        List<Recordatorio> r = servicio.recordatoriosPara(hoy);

        assertEquals(2, r.size());
        assertTrue(r.get(0).mensaje().contains("AND-1001"));
        assertTrue(r.get(1).mensaje().contains("AND-1001"));
    }

    @Test
    void unDiaBloqueadoPorElConductorNoGeneraAvisos() {
        conductor("3001234567");
        turista("3115556677");
        // El conductor bloquea unos días en su vehículo, pero no hay ninguna reserva
        Vehiculo bloqueado = new Vehiculo("juan@x.com", "Renault Duster", "estandar", 5);
        bloqueado.setId("v1");
        bloqueado.getPeriodosOcupados().add(new PeriodoOcupado(diaReserva, diaReserva.plusDays(2)));
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(bloqueado));

        assertTrue(servicio.recordatoriosPara(hoy).isEmpty());
    }

    @Test
    void siElVehiculoYaNoExisteSeSaltaLaReserva() {
        conductor("3001234567");
        turista("3115556677");
        reserva(diaReserva, "ana@x.com");
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.empty());

        assertTrue(servicio.recordatoriosPara(hoy).isEmpty());
    }

    @Test
    void elMensajeLlevaElCodigoDeLaReservaYUnaSolaVez() {
        conductor("3001234567");
        turista("3115556677");
        Reserva r = reserva(diaReserva, "ana@x.com");

        List<Recordatorio> avisos = servicio.recordatoriosPara(hoy);

        for (Recordatorio aviso : avisos) {
            assertTrue(aviso.mensaje().endsWith("Código de reserva: " + r.getCodigo() + "."));
            assertEquals(aviso.mensaje().indexOf("Código de reserva"), aviso.mensaje().lastIndexOf("Código de reserva"));
        }
    }

    @Test
    void sinCodigoElMensajeQuedaIgual() {
        assertEquals("Hola", RecordatorioService.conCodigo("Hola", null));
        assertEquals("Hola", RecordatorioService.conCodigo("Hola", "  "));
        assertEquals("Hola Código de reserva: AND-1.", RecordatorioService.conCodigo("Hola", "AND-1"));
    }
}
