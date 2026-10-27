package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.Reserva;
import com.example.Model.Usuario;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.ReservaRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
public class ContactoConductorService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReservaRepository reservaRepository;
    private final ConductorRepository conductorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public ContactoConductorService(ReservaRepository reservaRepository,
                                    ConductorRepository conductorRepository,
                                    VehiculoRepository vehiculoRepository,
                                    UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.conductorRepository = conductorRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ReservaConContacto> reservasDe(String correoTurista) {
        List<ReservaConContacto> lista = new ArrayList<>();
        for (Reserva reserva : reservaRepository.findByCorreoTurista(correoTurista)) {
            lista.add(conContacto(reserva));
        }
        lista.sort(Comparator.comparing((ReservaConContacto r) -> r.reserva().getFechaInicio(),
                Comparator.nullsLast(Comparator.naturalOrder())));
        return lista;
    }

    public ReservaConContacto conContacto(Reserva reserva) {
        Optional<Conductor> conductor = conductorRepository.findByCorreo(reserva.getCorreoConductor());
        String modelo = reserva.getVehiculoId() == null ? null
                : vehiculoRepository.findById(reserva.getVehiculoId()).map(Vehiculo::getModelo).orElse(null);
        Usuario turista = usuarioRepository.findByCorreo(reserva.getCorreoTurista()).orElse(null);

        String nombreConductor = conductor.map(Conductor::getNombre).orElse(null);
        String enlace = null;
        if (conductor.isPresent()) {
            String mensaje = mensajeReserva(reserva, turista, nombreConductor, modelo);
            enlace = WhatsAppEnlace.crear(conductor.get().getTelefono(), mensaje);
        }
        return new ReservaConContacto(reserva, nombreConductor, modelo, enlace);
    }

    static String mensajeReserva(Reserva reserva, Usuario turista, String nombreConductor, String modelo) {
        StringBuilder sb = new StringBuilder("Hola");
        if (nombreConductor != null && !nombreConductor.isBlank()) {
            sb.append(" ").append(nombreConductor.trim());
        }
        sb.append(", soy ");
        sb.append(turista == null || turista.getNombre() == null || turista.getNombre().isBlank()
                ? "tu cliente" : turista.getNombre().trim());
        sb.append(". Te escribo por mi reserva ").append(reserva.getCodigo()).append(" en Andén.");

        if (modelo != null && !modelo.isBlank()) {
            sb.append("\nVehículo: ").append(modelo.trim());
        }
        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            sb.append("\nFechas: del ").append(reserva.getFechaInicio().format(FECHA))
                    .append(" al ").append(reserva.getFechaFin().format(FECHA));
        }
        sb.append("\nPasajeros: ").append(reserva.getPasajeros());
        if (reserva.getTrayectos() != null && !reserva.getTrayectos().isBlank()) {
            sb.append("\nTrayectos: ").append(reserva.getTrayectos().trim());
        }
        if (turista != null && turista.getTelefono() != null && !turista.getTelefono().isBlank()) {
            sb.append("\nMi teléfono: ").append(turista.getTelefono().trim());
        }
        sb.append("\n¿Podemos hablar de mis planes y horarios?");
        return sb.toString();
    }
}
