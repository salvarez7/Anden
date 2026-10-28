package com.example.Controller;

import com.example.Model.Factura;
import com.example.Model.Reserva;
import com.example.Repository.ConductorRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.VehiculoRepository;
import com.example.Service.CalculadoraTarifa;
import com.example.Service.FacturaService;
import com.example.Service.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.Optional;

// después del pago el turista ve su factura y la puede imprimir
@Controller
public class FacturaController {

    private final ReservaService reservaService;
    private final FacturaService facturaService;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;

    public FacturaController(ReservaService reservaService, FacturaService facturaService,
                             VehiculoRepository vehiculoRepository, ConductorRepository conductorRepository,
                             UsuarioRepository usuarioRepository) {
        this.reservaService = reservaService;
        this.facturaService = facturaService;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/reservas/{codigo}/factura")
    public String verFactura(@PathVariable String codigo, Principal principal, Model model) {

        Optional<Reserva> encontrada = reservaService.buscarPorCodigo(codigo)
                .filter(r -> principal.getName().equals(r.getCorreoTurista()));
        if (encontrada.isEmpty()) {
            return "redirect:/vehiculos";
        }
        Reserva reserva = encontrada.get();
        Optional<Factura> factura = facturaService.facturaDe(reserva);
        if (factura.isEmpty()) {
            return "redirect:/reservas/" + codigo;
        }
        model.addAttribute("factura", factura.get());
        model.addAttribute("reserva", reserva);
        model.addAttribute("saldo", FacturaService.saldo(reserva, factura.get()));
        model.addAttribute("dias", CalculadoraTarifa.dias(reserva.getFechaInicio(), reserva.getFechaFin()));
        model.addAttribute("vehiculo", vehiculoRepository.findById(reserva.getVehiculoId()).orElse(null));
        model.addAttribute("conductor", conductorRepository.findByCorreo(reserva.getCorreoConductor()).orElse(null));
        model.addAttribute("turista", usuarioRepository.findByCorreo(reserva.getCorreoTurista()).orElse(null));
        model.addAttribute("aviso", FacturaService.AVISO);
        return "factura";
    }
}
