package com.example.Controller;

import com.example.Model.Reserva;
import com.example.Repository.ConductorRepository;
import com.example.Repository.ReservaRepository;
import com.example.Service.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Controller
public class ServiciosConductorController {

    private final ReservaRepository reservaRepository;
    private final ConductorRepository conductorRepository;
    private final ReservaService reservaService;

    public ServiciosConductorController(ReservaRepository reservaRepository,
                                        ConductorRepository conductorRepository,
                                        ReservaService reservaService) {
        this.reservaRepository = reservaRepository;
        this.conductorRepository = conductorRepository;
        this.reservaService = reservaService;
    }

    @GetMapping("/conductor/servicios")
    public String servicios(@RequestParam(value = "guardado", required = false) String guardado,
                            Principal principal, Model model) {
        if (!esConductor(principal)) {
            return "redirect:/";
        }
        return preparar(principal.getName(), guardado, List.of(), model);
    }

    @PostMapping("/conductor/servicios/{codigo}/valor-final")
    public String registrar(@PathVariable String codigo,
                            @RequestParam(value = "kilometros", required = false) Integer kilometros,
                            @RequestParam(value = "horas", required = false) Integer horas,
                            Principal principal, Model model) {
        if (!esConductor(principal)) {
            return "redirect:/";
        }
        try {
            reservaService.registrarValorFinal(codigo, principal.getName(), kilometros, horas);
            return "redirect:/conductor/servicios?guardado=" + codigo;
        } catch (IllegalArgumentException e) {
            return preparar(principal.getName(), null, List.of(e.getMessage().split("\n")), model);
        }
    }

    private boolean esConductor(Principal principal) {
        return conductorRepository.findByCorreo(principal.getName()).isPresent();
    }

    private String preparar(String correoConductor, String guardado, List<String> errores, Model model) {
        LocalDate hoy = LocalDate.now();
        List<ServicioConductor> servicios = new ArrayList<>();
        for (Reserva reserva : reservaRepository.findByCorreoConductor(correoConductor)) {
            
            boolean terminada = reserva.getFechaFin() != null && !hoy.isBefore(reserva.getFechaFin());
            servicios.add(new ServicioConductor(reserva, terminada));
        }
        servicios.sort(Comparator.comparing((ServicioConductor s) -> s.reserva().getFechaInicio(),
                Comparator.nullsLast(Comparator.reverseOrder())));

        model.addAttribute("servicios", servicios);
        model.addAttribute("guardado", guardado);
        model.addAttribute("errores", errores);
        return "servicios-conductor";
    }
}
