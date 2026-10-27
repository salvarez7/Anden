package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.Gama;
import com.example.Model.PeriodoOcupado;
import com.example.Model.Reserva;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.ReservaRepository;
import com.example.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        // Sprint 6 (H12)
        String errorTrayectos = CalculadoraTarifa.validarNumeroTrayectos(datos.numeroTrayectos(), false);
        if (errorTrayectos != null) {
            errores.add(errorTrayectos);
        }
        String errorFuera = CalculadoraTarifa.validarTrayectosFuera(datos.trayectosFuera(), false);
        if (errorFuera != null) {
            errores.add(errorFuera);
        }
        String errorHoras = CalculadoraTarifa.validarHorasPorDia(datos.horasPorDia(), false);
        if (errorHoras != null) {
            errores.add(errorHoras);
        }
        String errorHoraInicio = CalculadoraTarifa.validarHoraInicio(datos.horaInicio(), false);
        if (errorHoraInicio != null) {
            errores.add(errorHoraInicio);
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
        // Sprint 6 (H12): se guarda el estimado para compararlo con el valor final (H13) y la factura (H14)
        int numeroTrayectos = datos.numeroTrayectos() == null ? 0 : datos.numeroTrayectos();
        int trayectosFuera = datos.trayectosFuera() == null ? 0 : datos.trayectosFuera();
        reserva.setNumeroTrayectos(numeroTrayectos);
        reserva.setTrayectosFuera(trayectosFuera);
        reserva.setHorasPorDia(datos.horasPorDia());
        reserva.setHoraInicio(datos.horaInicio());
        CalculadoraTarifa.calcular(vehiculo.getGama(), datos.fechaInicio(), datos.fechaFin(),
                        datos.horasPorDia(), datos.pasajeros(), numeroTrayectos, trayectosFuera, datos.horaInicio())
                .ifPresent(tarifa -> reserva.setValorEstimado(tarifa.total()));
        reservaRepository.save(reserva);

        
        vehiculo.getPeriodosOcupados().add(
                new PeriodoOcupado(datos.fechaInicio(), datos.fechaFin(), datos.correoTurista()));
        vehiculoRepository.save(vehiculo);

        return reserva;
    }

    // Sprint 6 (H13): el conductor registra el valor final cuando termina el servicio.
    // Se calcula con los kilómetros y las horas reales. Si ya había uno, se actualiza.
    public Reserva registrarValorFinal(String codigo, String correoConductor, Integer kilometros, Integer horas) {
        return registrarValorFinal(codigo, correoConductor, kilometros, horas, LocalDate.now());
    }

    Reserva registrarValorFinal(String codigo, String correoConductor, Integer kilometros, Integer horas,
                                LocalDate hoy) {
        Reserva reserva = reservaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos esa reserva."));
        if (correoConductor == null || !correoConductor.equals(reserva.getCorreoConductor())) {
            throw new IllegalArgumentException("Esta reserva no es de tu vehículo.");
        }
        if (!Reserva.CONFIRMADA.equals(reserva.getEstado())) {
            throw new IllegalArgumentException("Solo se registra el valor final de reservas confirmadas.");
        }
        // RF-33: el valor se registra una vez concluido el servicio
        if (hoy.isBefore(reserva.getFechaFin())) {
            throw new IllegalArgumentException("El valor final se registra cuando termina el servicio (desde el "
                    + reserva.getFechaFin() + ").");
        }

        int dias = CalculadoraTarifa.dias(reserva.getFechaInicio(), reserva.getFechaFin());
        List<String> errores = new ArrayList<>();
        String errorKilometros = ValorFinalCalculadora.validarKilometros(kilometros);
        if (errorKilometros != null) {
            errores.add(errorKilometros);
        }
        String errorHoras = ValorFinalCalculadora.validarHoras(horas, dias);
        if (errorHoras != null) {
            errores.add(errorHoras);
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }

        Gama gama = vehiculoRepository.findById(reserva.getVehiculoId())
                .map(Vehiculo::getGama)
                .map(Gama::desde)
                .orElse(null);
        if (gama == null) {
            throw new IllegalArgumentException("No pudimos calcular el valor: falta la gama del vehículo.");
        }

        reserva.setKilometrosReales(kilometros);
        reserva.setHorasReales(horas);
        reserva.setValorFinal(ValorFinalCalculadora.calcular(gama, dias, horas, reserva.getPasajeros(), kilometros));
        reserva.setFechaValorFinal(LocalDateTime.now());
        reservaRepository.save(reserva);
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
