package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.PeriodoOcupado;
import com.example.Model.Usuario;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecordatorioService {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioService.class);
    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");
    static final int DIAS_ANTES = 3;

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;

    public RecordatorioService(VehiculoRepository vehiculoRepository,
                               ConductorRepository conductorRepository,
                               UsuarioRepository usuarioRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "America/Bogota")
    public void enviarRecordatoriosDeHoy() {
        List<Recordatorio> recordatorios = recordatoriosPara(LocalDate.now(COLOMBIA));
        for (Recordatorio r : recordatorios) {
            log.info("Recordatorio para {} {} ({}): {} | WhatsApp: {}",
                    r.paraQuien(), r.nombre(), r.correo(), r.mensaje(), r.enlaceWhatsApp());
        }
        log.info("Recordatorios de reservas enviados hoy: {}", recordatorios.size());
    }

    // Arma los recordatorios de las reservas que son 3 días después de hoy
    public List<Recordatorio> recordatoriosPara(LocalDate hoy) {
        LocalDate diaDeLaReserva = hoy.plusDays(DIAS_ANTES);
        List<Recordatorio> recordatorios = new ArrayList<>();

        for (Vehiculo vehiculo : vehiculoRepository.findAll()) {
            for (PeriodoOcupado periodo : vehiculo.getPeriodosOcupados()) {
                if (!diaDeLaReserva.equals(periodo.getFechaInicio())) {
                    continue;
                }

                // Al conductor
                Optional<Conductor> conductor = conductorRepository.findByCorreo(vehiculo.getCorreoConductor());
                if (conductor.isPresent()) {
                    Conductor c = conductor.get();
                    String mensaje = mensajeParaConductor(c.getNombre(), vehiculo, periodo);
                    recordatorios.add(new Recordatorio("CONDUCTOR", c.getNombre(), c.getCorreo(), vehiculo, periodo,
                            mensaje, WhatsAppEnlace.crear(c.getTelefono(), mensaje)));
                }

                // Al turista
                if (periodo.getCorreoTurista() != null) {
                    Optional<Usuario> turista = usuarioRepository.findByCorreo(periodo.getCorreoTurista());
                    if (turista.isPresent()) {
                        Usuario t = turista.get();
                        String nombreConductor = conductor.map(Conductor::getNombre).orElse(null);
                        String mensaje = mensajeParaTurista(t.getNombre(), nombreConductor, vehiculo, periodo);
                        recordatorios.add(new Recordatorio("TURISTA", t.getNombre(), t.getCorreo(), vehiculo, periodo,
                                mensaje, WhatsAppEnlace.crear(t.getTelefono(), mensaje)));
                    }
                }
            }
        }
        return recordatorios;
    }

    static String mensajeParaConductor(String nombreConductor, Vehiculo vehiculo, PeriodoOcupado periodo) {
        return "Hola" + nombreCorto(nombreConductor) + ", te recordamos que en " + DIAS_ANTES
                + " días tienes un servicio con Andén en el vehículo " + vehiculo.getModelo()
                + ", del " + periodo.getFechaInicio() + " al " + periodo.getFechaFin() + ".";
    }

    static String mensajeParaTurista(String nombreTurista, String nombreConductor,
                                     Vehiculo vehiculo, PeriodoOcupado periodo) {
        String conQuien = nombreConductor == null ? "" : " con " + nombreConductor.trim();
        return "Hola" + nombreCorto(nombreTurista) + ", te recordamos que en " + DIAS_ANTES
                + " días es tu reserva en Andén" + conQuien + " (vehículo " + vehiculo.getModelo()
                + "), del " + periodo.getFechaInicio() + " al " + periodo.getFechaFin() + ".";
    }

    private static String nombreCorto(String nombre) {
        return nombre == null || nombre.isBlank() ? "" : " " + nombre.trim();
    }
}
