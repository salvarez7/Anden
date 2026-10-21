package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.PeriodoOcupado;
import com.example.Model.Reserva;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.ReservaRepository;
import com.example.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservaService {

    static final int MAXIMO_CARACTERES_TRAYECTOS = 300;

    private final ReservaRepository reservaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final VehiculoService vehiculoService;

    public ReservaService(ReservaRepository reservaRepository,
                          VehiculoRepository vehiculoRepository,
                          ConductorRepository conductorRepository,
                          VehiculoService vehiculoService) {
        this.reservaRepository = reservaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.vehiculoService = vehiculoService;
    }

    
    public List<String> validar(DatosReserva datos) {
        List<String> errores = new ArrayList<>();

        if (datos.correoTurista() == null || datos.correoTurista().isBlank()) {
            errores.add("Inicia sesión para reservar.");
        }

        Optional<Vehiculo> encontrado = datos.vehiculoId() == null
                ? Optional.empty()
                : vehiculoRepository.findById(datos.vehiculoId());
        if (encontrado.isEmpty()) {
            errores.add("No encontramos ese vehículo.");
            return errores;
        }
        Vehiculo vehiculo = encontrado.get();

        
        Optional<Conductor> conductor = conductorRepository.findByCorreo(vehiculo.getCorreoConductor());
        if (conductor.isEmpty() || !"APROBADO".equals(conductor.get().getEstado())) {
            errores.add("Este conductor no está disponible para reservas.");
        }

      
        LocalDate inicio = datos.fechaInicio();
        LocalDate fin = datos.fechaFin();
        if (inicio == null || fin == null) {
            errores.add("Elige la fecha de inicio y la de fin.");
        } else if (inicio.isBefore(LocalDate.now())) {
            errores.add("La fecha de inicio no puede estar en el pasado.");
        } else if (fin.isBefore(inicio)) {
            errores.add("La fecha de fin debe ser igual o posterior a la de inicio.");
        } else if (!vehiculoService.estaLibre(vehiculo, inicio, fin)) {
            errores.add("Este vehículo no está disponible en esas fechas. Prueba con otras fechas.");
        }

        
        Integer pasajeros = datos.pasajeros();
        if (pasajeros == null || pasajeros < 1) {
            errores.add("Indica cuántos pasajeros viajan (mínimo 1).");
        } else if (pasajeros > vehiculo.getNumeroAsientos()) {
            errores.add("Este vehículo tiene capacidad para " + vehiculo.getNumeroAsientos() + " pasajeros.");
        }

        String trayectos = datos.trayectos() == null ? "" : datos.trayectos().trim();
        if (trayectos.isEmpty()) {
            errores.add("Cuéntanos los trayectos que quieres hacer.");
        } else if (trayectos.length() > MAXIMO_CARACTERES_TRAYECTOS) {
            errores.add("Los trayectos no pueden pasar de " + MAXIMO_CARACTERES_TRAYECTOS + " caracteres.");
        }

        return errores;
    }

   
    public Reserva crearReserva(DatosReserva datos) {
        List<String> errores = validar(datos);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }

        Vehiculo vehiculo = vehiculoRepository.findById(datos.vehiculoId()).get();

        Reserva reserva = new Reserva(
                codigoNuevo(),
                vehiculo.getId(),
                vehiculo.getCorreoConductor(),
                datos.correoTurista(),
                datos.fechaInicio(),
                datos.fechaFin(),
                datos.pasajeros(),
                datos.trayectos().trim());
        reservaRepository.save(reserva);

        
        vehiculo.getPeriodosOcupados().add(
                new PeriodoOcupado(datos.fechaInicio(), datos.fechaFin(), datos.correoTurista()));
        vehiculoRepository.save(vehiculo);

        return reserva;
    }

    public Optional<Reserva> buscarPorCodigo(String codigo) {
        return reservaRepository.findByCodigo(codigo);
    }

  
    private String codigoNuevo() {
        for (int intento = 0; intento < 10; intento++) {
            String codigo = "AND-" + (1000 + ThreadLocalRandom.current().nextInt(9000));
            if (!reservaRepository.existsByCodigo(codigo)) {
                return codigo;
            }
        }
        return "AND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
