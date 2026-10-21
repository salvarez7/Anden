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
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservaServiceTest {

    
    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private final VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
    private final ConductorRepository conductorRepository = mock(ConductorRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);

    private final ReservaService servicio = new ReservaService(
            reservaRepository, vehiculoRepository, conductorRepository,
            new VehiculoService(vehiculoRepository));

    private final LocalDate inicio = LocalDate.now().plusDays(10);
    private final LocalDate fin = LocalDate.now().plusDays(13);

    private Vehiculo carro;
    private Conductor juan;

    @BeforeEach
    void preparar() {
        carro = new Vehiculo("juan@correo.com", "Chevrolet Spark", "economico", 4);
        carro.setId("v1");
        juan = new Conductor();
        juan.setCorreo("juan@correo.com");
        juan.setNombre("Juan Pérez");
        juan.setTelefono("3001234567");
        juan.setEstado("APROBADO");
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(carro));
        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(juan));
    }

    private DatosReserva datos(int pasajeros, LocalDate desde, LocalDate hasta) {
        return new DatosReserva("v1", "ana@correo.com", desde, hasta, pasajeros, "Aeropuerto a hotel");
    }

    private String errorDe(DatosReserva datos) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.crearReserva(datos));
        return e.getMessage();
    }

    @Test
    void reservaValidaSeGuardaConSusDatos() {
        Reserva r = servicio.crearReserva(datos(3, inicio, fin));

        ArgumentCaptor<Reserva> guardada = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).save(guardada.capture());
        assertEquals(r, guardada.getValue());
        assertEquals("v1", r.getVehiculoId());
        assertEquals("juan@correo.com", r.getCorreoConductor());
        assertEquals("ana@correo.com", r.getCorreoTurista());
        assertEquals(3, r.getPasajeros());
        assertEquals("CONFIRMADA", r.getEstado());
    }

    @Test
    void elCodigoDelTicketTieneElFormatoDelPrototipo() {
        Reserva r = servicio.crearReserva(datos(2, inicio, fin));
        assertTrue(r.getCodigo().matches("AND-\\d{4}"));
    }

    @Test
    void despuesDeReservarElVehiculoQuedaOcupadoEnEsasFechas() {
        servicio.crearReserva(datos(2, inicio, fin));

        assertEquals(1, carro.getPeriodosOcupados().size());
        PeriodoOcupado periodo = carro.getPeriodosOcupados().get(0);
        assertEquals(inicio, periodo.getFechaInicio());
        assertEquals(fin, periodo.getFechaFin());
        assertEquals("ana@correo.com", periodo.getCorreoTurista());
        verify(vehiculoRepository).save(carro);
    }

    @Test
    void unaSegundaReservaEnLasMismasFechasEsRechazada() {
        servicio.crearReserva(datos(2, inicio, fin));
        String error = errorDe(datos(2, inicio.plusDays(1), fin.plusDays(1)));
        assertTrue(error.contains("no está disponible en esas fechas"));
    }

    @Test
    void masPasajerosQueLosPuestosNoSeGuarda() {
        String error = errorDe(datos(5, inicio, fin));
        assertTrue(error.contains("capacidad para 4 pasajeros"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void justoLosPuestosDelVehiculoSiSePermite() {
        Reserva r = servicio.crearReserva(datos(4, inicio, fin));
        assertEquals(4, r.getPasajeros());
    }

    @Test
    void ceroPasajerosNoSeGuarda() {
        String error = errorDe(datos(0, inicio, fin));
        assertTrue(error.contains("mínimo 1"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void fechaDeInicioEnElPasadoNoSeGuarda() {
        String error = errorDe(datos(2, LocalDate.now().minusDays(1), fin));
        assertTrue(error.contains("no puede estar en el pasado"));
    }

    @Test
    void fechaDeFinAnteriorAlInicioNoSeGuarda() {
        String error = errorDe(datos(2, fin, inicio));
        assertTrue(error.contains("posterior a la de inicio"));
    }

    @Test
    void unaReservaDeUnSoloDiaSiSePermite() {
        Reserva r = servicio.crearReserva(datos(2, inicio, inicio));
        assertEquals(inicio, r.getFechaFin());
    }

    @Test
    void vehiculoOcupadoNoSeReserva() {
        carro.getPeriodosOcupados().add(new PeriodoOcupado(inicio.plusDays(1), inicio.plusDays(5)));
        String error = errorDe(datos(2, inicio, fin));
        assertTrue(error.contains("no está disponible en esas fechas"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void sinTrayectosNoSeGuarda() {
        DatosReserva sinTrayectos = new DatosReserva("v1", "ana@correo.com", inicio, fin, 2, "   ");
        assertTrue(errorDe(sinTrayectos).contains("trayectos que quieres hacer"));
    }

    @Test
    void trayectosDeMasDe300CaracteresNoSeGuardan() {
        DatosReserva largo = new DatosReserva("v1", "ana@correo.com", inicio, fin, 2, "a".repeat(301));
        assertTrue(errorDe(largo).contains("no pueden pasar de 300"));
    }

    @Test
    void conductorPendienteDeVerificacionNoSePuedeReservar() {
        juan.setEstado("PENDIENTE_VERIFICACION");
        String error = errorDe(datos(2, inicio, fin));
        assertTrue(error.contains("conductor no está disponible"));
    }

    @Test
    void vehiculoQueNoExisteNoSeGuarda() {
        when(vehiculoRepository.findById("nada")).thenReturn(Optional.empty());
        DatosReserva datos = new DatosReserva("nada", "ana@correo.com", inicio, fin, 2, "Aeropuerto");
        assertTrue(errorDe(datos).contains("No encontramos ese vehículo"));
    }

    @Test
    void sinCorreoDelTuristaNoSeGuarda() {
        DatosReserva anonimo = new DatosReserva("v1", " ", inicio, fin, 2, "Aeropuerto");
        assertTrue(errorDe(anonimo).contains("Inicia sesión"));
    }

    @Test
    void siFallaVariasCosasSeMuestranTodosLosMensajes() {
        DatosReserva malo = new DatosReserva("v1", "ana@correo.com", fin, inicio, 9, "");
        String error = errorDe(malo);
        assertTrue(error.contains("posterior a la de inicio"));
        assertTrue(error.contains("capacidad para 4 pasajeros"));
        assertTrue(error.contains("trayectos que quieres hacer"));
    }

    @Test
    void buscarPorCodigoDevuelveLaReserva() {
        Reserva guardada = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                inicio, fin, 2, "Aeropuerto");
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(guardada));
        assertEquals(guardada, servicio.buscarPorCodigo("AND-1234").get());
    }

    @Test
    void laReservaCreadaHaceQueElRecordatorioAvisaAlTurista() {
       
        when(usuarioRepository.findByCorreo("ana@correo.com"))
                .thenReturn(Optional.of(new Usuario("Ana", "ana@correo.com", "3109876543", "x", "TURISTA")));
        when(vehiculoRepository.findAll()).thenReturn(List.of(carro));
        RecordatorioService recordatorios =
                new RecordatorioService(vehiculoRepository, conductorRepository, usuarioRepository);

        servicio.crearReserva(datos(2, inicio, fin));

        List<Recordatorio> avisos = recordatorios.recordatoriosPara(inicio.minusDays(3));
        assertTrue(avisos.stream().anyMatch(a -> a.paraQuien().equals("TURISTA")));
        assertTrue(avisos.stream().anyMatch(a -> a.paraQuien().equals("CONDUCTOR")));
    }
}
