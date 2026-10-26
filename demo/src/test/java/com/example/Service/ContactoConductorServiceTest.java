package com.example.Service;

import com.example.Model.Conductor;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContactoConductorServiceTest {

    private ReservaRepository reservaRepository;
    private ConductorRepository conductorRepository;
    private VehiculoRepository vehiculoRepository;
    private UsuarioRepository usuarioRepository;
    private ContactoConductorService servicio;

    private Conductor juan;
    private Usuario ana;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        reservaRepository = mock(ReservaRepository.class);
        conductorRepository = mock(ConductorRepository.class);
        vehiculoRepository = mock(VehiculoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        servicio = new ContactoConductorService(reservaRepository, conductorRepository,
                vehiculoRepository, usuarioRepository);

        juan = new Conductor();
        juan.setCorreo("juan@correo.com");
        juan.setNombre("Juan Pérez");
        juan.setTelefono("3001234567");
        ana = new Usuario("Ana", "ana@correo.com", "3109876543", "x", "TURISTA");
        Vehiculo duster = new Vehiculo("juan@correo.com", "Renault Duster", "estandar", 5);
        duster.setId("v1");

        reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                LocalDate.of(2027, 7, 21), LocalDate.of(2027, 7, 24), 3, "Aeropuerto a hotel");

        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(juan));
        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(ana));
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(duster));
    }

    @Test
    void elMensajeTieneLosDatosDelTuristaYDeLaReserva() {
        String mensaje = ContactoConductorService.mensajeReserva(reserva, ana, "Juan Pérez", "Renault Duster");

        assertTrue(mensaje.startsWith("Hola Juan Pérez, soy Ana. Te escribo por mi reserva AND-1234 en Andén."));
        assertTrue(mensaje.contains("Vehículo: Renault Duster"));
        assertTrue(mensaje.contains("Fechas: del 21/07/2027 al 24/07/2027"));
        assertTrue(mensaje.contains("Pasajeros: 3"));
        assertTrue(mensaje.contains("Trayectos: Aeropuerto a hotel"));
        assertTrue(mensaje.contains("Mi teléfono: 3109876543"));
    }

    @Test
    void elEnlaceAbreElChatDelConductorDeLaReserva() {
        ReservaConContacto r = servicio.conContacto(reserva);

        assertEquals("Juan Pérez", r.nombreConductor());
        assertEquals("Renault Duster", r.modeloVehiculo());
        assertTrue(r.enlaceWhatsApp().startsWith("https://wa.me/573001234567?text=Hola%20Juan"));
        assertTrue(r.enlaceWhatsApp().contains("AND-1234"));
        assertTrue(r.enlaceWhatsApp().contains("%0A")); // los saltos de línea del mensaje
    }

    @Test
    void siElConductorNoTieneUnNumeroValidoNoHayEnlace() {
        juan.setTelefono("123");
        assertNull(servicio.conContacto(reserva).enlaceWhatsApp());
    }

    @Test
    void siNoEncuentraAlConductorNoHayEnlace() {
        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.empty());
        ReservaConContacto r = servicio.conContacto(reserva);
        assertNull(r.enlaceWhatsApp());
        assertNull(r.nombreConductor());
    }

    @Test
    void sinDatosDelTuristaIgualArmaElMensaje() {
        String mensaje = ContactoConductorService.mensajeReserva(reserva, null, null, null);
        assertTrue(mensaje.startsWith("Hola, soy tu cliente. Te escribo por mi reserva AND-1234"));
        assertTrue(!mensaje.contains("Mi teléfono"));
    }

    @Test
    void lasReservasDelTuristaSalenOrdenadasPorFecha() {
        Reserva despues = new Reserva("AND-2000", "v1", "juan@correo.com", "ana@correo.com",
                LocalDate.of(2027, 9, 1), LocalDate.of(2027, 9, 2), 1, "Centro");
        when(reservaRepository.findByCorreoTurista("ana@correo.com")).thenReturn(List.of(despues, reserva));

        List<ReservaConContacto> lista = servicio.reservasDe("ana@correo.com");

        assertEquals(2, lista.size());
        assertEquals("AND-1234", lista.get(0).reserva().getCodigo());
        assertEquals("AND-2000", lista.get(1).reserva().getCodigo());
    }
}
