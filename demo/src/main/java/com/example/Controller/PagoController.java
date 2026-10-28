package com.example.Controller;

import com.example.Model.Pago;
import com.example.Model.Reserva;
import com.example.Model.Vehiculo;
import com.example.Repository.VehiculoRepository;
import com.example.Service.PagoService;
import com.example.Service.ReservaService;
import com.example.Service.TarifaEstimada;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

// Pago simulado de una reserva
@Controller
public class PagoController {

    private final ReservaService reservaService;
    private final PagoService pagoService;
    private final VehiculoRepository vehiculoRepository;

    public PagoController(ReservaService reservaService, PagoService pagoService,
                          VehiculoRepository vehiculoRepository) {
        this.reservaService = reservaService;
        this.pagoService = pagoService;
        this.vehiculoRepository = vehiculoRepository;
    }

    @GetMapping("/reservas/{codigo}/pago")
    public String formulario(@PathVariable String codigo, Principal principal, Model model) {
        Optional<Reserva> encontrada = reservaService.buscarPorCodigo(codigo);
        if (encontrada.isEmpty() || !principal.getName().equals(encontrada.get().getCorreoTurista())) {
            return "redirect:/vehiculos";
        }
        Reserva reserva = encontrada.get();
        Long monto = PagoService.montoDe(reserva);
        if (Reserva.PAGADA.equals(reserva.getEstadoPago()) || monto == null || monto <= 0) {
            return "redirect:/reservas/" + codigo;
        }

        String modelo = reserva.getVehiculoId() == null ? null
                : vehiculoRepository.findById(reserva.getVehiculoId()).map(Vehiculo::getModelo).orElse(null);

        model.addAttribute("reserva", reserva);
        model.addAttribute("modelo", modelo);
        model.addAttribute("monto", TarifaEstimada.pesos(monto));
        model.addAttribute("total", TarifaEstimada.pesos(PagoService.totalDe(reserva)));
        return "pago";
    }

    @PostMapping("/reservas/{codigo}/pago")
    public String pagar(@PathVariable String codigo, @RequestParam(required = false) String metodo,
                        @RequestParam(required = false) String titular,
                        Principal principal, RedirectAttributes redirect) {
        try {
            Pago pago = pagoService.pagar(codigo, principal.getName(), metodo, titular);
            redirect.addFlashAttribute("mensajePago",
                    "¡Pago exitoso! Recibimos " + TarifaEstimada.pesos(pago.getMonto())
                            + " como anticipo del 10% por transferencia desde " + PagoService.etiqueta(pago.getMetodo())
                            + ". Tu reserva " + codigo + " quedó pagada. Referencia: " + pago.getReferencia()
                            + ". Te enviamos el recibo a tu correo (simulado).");
            return "redirect:/reservas/" + codigo;
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("errorPago", e.getMessage());
            return "redirect:/reservas/" + codigo + "/pago";
        }
    }
}