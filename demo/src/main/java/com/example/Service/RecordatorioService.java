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
        LocalDate hoy = LocalDate.now(COLOMBIA);
        int total = 0;

        for (TipoNotificacion tipo : TipoNotificacion.values()) {
            List<Recordatorio> recordatorios = recordatoriosPara(hoy, tipo);
            for (Recordatorio r : recordatorios) {
                log.info("{} para {} {} ({}): {} | WhatsApp: {}",
                        r.tipo(), r.paraQuien(), r.nombre(), r.correo(), r.mensaje(), r.enlaceWhatsApp());
            }
            total += recordatorios.size();
        }
        log.info("Notificaciones de reservas enviadas hoy: {}", total);
    }

    // Avisos de confirmación del servicio (3 días antes)
    public List<Recordatorio> recordatoriosPara(LocalDate hoy) {
        return recordatoriosPara(hoy, TipoNotificacion.CONFIRMACION);
    }

    // Arma los avisos de las reservas que empiezan dentro de "tipo.getDiasAntes()" días
    public List<Recordatorio> recordatoriosPara(LocalDate hoy, TipoNotificacion tipo) {
        LocalDate diaDeLaReserva = hoy.plusDays(tipo.getDiasAntes());
        boolean esCancelacion = tipo == TipoNotificacion.CANCELACION;
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
                    String mensaje = esCancelacion
                            ? mensajeCancelacionConductor(c.getNombre(), vehiculo, periodo)
                            : mensajeParaConductor(c.getNombre(), vehiculo, periodo);
                    recordatorios.add(new Recordatorio("CONDUCTOR", c.getNombre(), c.getCorreo(), vehiculo, periodo,
                            mensaje, WhatsAppEnlace.crear(c.getTelefono(), mensaje), tipo));
                }

                // Al turista
                if (periodo.getCorreoTurista() != null) {
                    Optional<Usuario> turista = usuarioRepository.findByCorreo(periodo.getCorreoTurista());
                    if (turista.isPresent()) {
                        Usuario t = turista.get();
                        String nombreConductor = conductor.map(Conductor::getNombre).orElse(null);
                        String mensaje = esCancelacion
                                ? mensajeCancelacionTurista(t.getNombre(), nombreConductor, vehiculo, periodo)
                                : mensajeParaTurista(t.getNombre(), nombreConductor, vehiculo, periodo);
                        recordatorios.add(new Recordatorio("TURISTA", t.getNombre(), t.getCorreo(), vehiculo, periodo,
                                mensaje, WhatsAppEnlace.crear(t.getTelefono(), mensaje), tipo));
                    }
                }
            }
        }
        return recordatorios;
    }

    // ---- Confirmación del servicio (3 días antes) ----

    static String mensajeParaConductor(String nombreConductor, Vehiculo vehiculo, PeriodoOcupado periodo) {
        return "Hola" + nombreCorto(nombreConductor) + ", te recordamos que en "
                + TipoNotificacion.CONFIRMACION.getDiasAntes()
                + " días tienes un servicio con Andén en el vehículo " + vehiculo.getModelo()
                + ", del " + periodo.getFechaInicio() + " al " + periodo.getFechaFin() + ".";
    }

    static String mensajeParaTurista(String nombreTurista, String nombreConductor,
                                     Vehiculo vehiculo, PeriodoOcupado periodo) {
        String conQuien = nombreConductor == null ? "" : " con " + nombreConductor.trim();
        return "Hola" + nombreCorto(nombreTurista) + ", te recordamos que en "
                + TipoNotificacion.CONFIRMACION.getDiasAntes()
                + " días es tu reserva en Andén" + conQuien + " (vehículo " + vehiculo.getModelo()
                + "), del " + periodo.getFechaInicio() + " al " + periodo.getFechaFin() + ".";
    }

    // ---- Recordatorio de cancelación (2 semanas antes) ----

    static String mensajeCancelacionConductor(String nombreConductor, Vehiculo vehiculo, PeriodoOcupado periodo) {
        return "Hola" + nombreCorto(nombreConductor) + ", faltan 2 semanas para tu servicio con Andén en el vehículo "
                + vehiculo.getModelo() + ", del " + periodo.getFechaInicio() + " al " + periodo.getFechaFin()
                + ". Si no vas a poder cumplirlo, este es el momento de cancelar.";
    }

    static String mensajeCancelacionTurista(String nombreTurista, String nombreConductor,
                                            Vehiculo vehiculo, PeriodoOcupado periodo) {
        String conQuien = nombreConductor == null ? "" : " con " + nombreConductor.trim();
        return "Hola" + nombreCorto(nombreTurista) + ", faltan 2 semanas para tu reserva en Andén" + conQuien
                + " (vehículo " + vehiculo.getModelo() + "), del " + periodo.getFechaInicio() + " al "
                + periodo.getFechaFin() + ". Si necesitas cancelar, este es el momento de hacerlo.";
    }

    private static String nombreCorto(String nombre) {
        return nombre == null || nombre.isBlank() ? "" : " " + nombre.trim();
    }
}