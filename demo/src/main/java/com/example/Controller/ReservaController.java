package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Model.Reserva;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.VehiculoRepository;
import com.example.Service.DatosReserva;
import com.example.Service.ReservaService;
import com.example.Service.VehiculoService;
import com.example.Service.WhatsAppEnlace;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Controller
public class ReservaController {

    private static final int MAXIMO_CARACTERES_TRAYECTOS = 300;

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoService vehiculoService;
    private final ReservaService reservaService;

    public ReservaController(VehiculoRepository vehiculoRepository,
                             ConductorRepository conductorRepository,
                             UsuarioRepository usuarioRepository,
                             VehiculoService vehiculoService,
                             ReservaService reservaService) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoService = vehiculoService;
        this.reservaService = reservaService;
    }

    @GetMapping("/reservas/nueva")
    public String mostrarFormulario(@RequestParam String vehiculoId,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                    Model model) {
        FormularioReserva formulario = new FormularioReserva();
        formulario.setVehiculoId(vehiculoId);
        formulario.setFechaInicio(fechaInicio);
        formulario.setFechaFin(fechaFin);
        return preparar(formulario, new ArrayList<>(), false, null, model);
    }

    @PostMapping("/reservas/nueva")
    public String verificarFechas(@ModelAttribute("formulario") FormularioReserva formulario,
                                  Principal principal, Model model) {
        List<String> errores = new ArrayList<>();
        LocalDate inicio = formulario.getFechaInicio();
        LocalDate fin = formulario.getFechaFin();

        // 1) Fechas
        if (inicio == null || fin == null) {
            errores.add("Elige la fecha de inicio y la de fin.");
        } else if (inicio.isBefore(LocalDate.now())) {
            errores.add("La fecha de inicio no puede estar en el pasado.");
        } else if (fin.isBefore(inicio)) {
            errores.add("La fecha de fin debe ser igual o posterior a la de inicio.");
        } else if (formulario.getVehiculoId() != null
                && !vehiculoService.estaDisponible(formulario.getVehiculoId(), inicio, fin)) {
            errores.add("Este vehículo no está disponible en esas fechas. Prueba con otras fechas.");
        }

        
        Integer pasajeros = formulario.getPasajeros();
        if (pasajeros == null || pasajeros < 1) {
            errores.add("Indica cuántos pasajeros viajan (mínimo 1).");
        } else if (formulario.getVehiculoId() != null) {
            vehiculoRepository.findById(formulario.getVehiculoId()).ifPresent(vehiculo -> {
                if (pasajeros > vehiculo.getNumeroAsientos()) {
                    errores.add("Este vehículo tiene capacidad para " + vehiculo.getNumeroAsientos() + " pasajeros.");
                }
            });
        }

        
        String trayectos = formulario.getTrayectos() == null ? "" : formulario.getTrayectos().trim();
        if (trayectos.isEmpty()) {
            errores.add("Cuéntanos los trayectos que quieres hacer.");
        } else if (trayectos.length() > MAXIMO_CARACTERES_TRAYECTOS) {
            errores.add("Los trayectos no pueden pasar de " + MAXIMO_CARACTERES_TRAYECTOS + " caracteres.");
        }

        // Solo si todo está bien se arma el enlace de WhatsApp
        boolean fechasValidas = errores.isEmpty();
        String nombreTurista = usuarioRepository.findByCorreo(principal.getName())
                .map(u -> u.getNombre()).orElse(null);
        return preparar(formulario, errores, fechasValidas, nombreTurista, model);
    }

    
    @PostMapping("/reservas/confirmar")
    public String confirmar(@ModelAttribute("formulario") FormularioReserva formulario,
                            Principal principal, Model model) {
        DatosReserva datos = new DatosReserva(formulario.getVehiculoId(), principal.getName(),
                formulario.getFechaInicio(), formulario.getFechaFin(),
                formulario.getPasajeros(), formulario.getTrayectos());
        try {
            Reserva reserva = reservaService.crearReserva(datos);
            return "redirect:/reservas/" + reserva.getCodigo();
        } catch (IllegalArgumentException e) {
            List<String> errores = new ArrayList<>(List.of(e.getMessage().split("\n")));
            String nombreTurista = usuarioRepository.findByCorreo(principal.getName())
                    .map(u -> u.getNombre()).orElse(null);
            return preparar(formulario, errores, false, nombreTurista, model);
        }
    }

    
    @GetMapping("/reservas/{codigo}")
    public String verTicket(@PathVariable String codigo, Principal principal, Model model) {
        Optional<Reserva> encontrada = reservaService.buscarPorCodigo(codigo);
        if (encontrada.isEmpty() || !principal.getName().equals(encontrada.get().getCorreoTurista())) {
            return "redirect:/vehiculos";
        }
        Reserva reserva = encontrada.get();
        Optional<Vehiculo> vehiculo = vehiculoRepository.findById(reserva.getVehiculoId());
        Optional<Conductor> conductor = conductorRepository.findByCorreo(reserva.getCorreoConductor());
        String nombreTurista = usuarioRepository.findByCorreo(principal.getName())
                .map(u -> u.getNombre()).orElse(null);

        model.addAttribute("reserva", reserva);
        model.addAttribute("vehiculo", vehiculo.orElse(null));
        model.addAttribute("conductor", conductor.orElse(null));
        if (conductor.isPresent()) {
            String mensaje = WhatsAppEnlace.mensajeReserva(nombreTurista,
                    vehiculo.map(Vehiculo::getModelo).orElse(null),
                    reserva.getFechaInicio(), reserva.getFechaFin(),
                    reserva.getPasajeros(), reserva.getTrayectos());
            model.addAttribute("enlaceWhatsApp",
                    WhatsAppEnlace.crear(conductor.get().getTelefono(), mensaje));
        }
        return "reserva-confirmada";
    }

    private String preparar(FormularioReserva formulario, List<String> errores,
                            boolean fechasValidas, String nombreTurista, Model model) {
        if (formulario.getVehiculoId() == null || formulario.getVehiculoId().isBlank()) {
            return "redirect:/vehiculos";
        }
        Optional<Vehiculo> vehiculo = vehiculoRepository.findById(formulario.getVehiculoId());
        if (vehiculo.isEmpty()) {
            return "redirect:/vehiculos";
        }
        Optional<Conductor> conductor = conductorRepository.findByCorreo(vehiculo.get().getCorreoConductor());

        model.addAttribute("formulario", formulario);
        model.addAttribute("vehiculo", vehiculo.get());
        model.addAttribute("conductor", conductor.orElse(null));
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("errores", errores);
        model.addAttribute("fechasValidas", fechasValidas);

        
        if (fechasValidas && conductor.isPresent()) {
            String mensaje = WhatsAppEnlace.mensajeReserva(nombreTurista, vehiculo.get().getModelo(),
                    formulario.getFechaInicio(), formulario.getFechaFin(),
                    formulario.getPasajeros(), formulario.getTrayectos());
            model.addAttribute("enlaceWhatsApp",
                    WhatsAppEnlace.crear(conductor.get().getTelefono(), mensaje));
        }
        return "reserva";
    }


}