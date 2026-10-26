package com.example.Controller;

import com.example.Service.ContactoConductorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

// El turista ve sus reservas y desde cada una puede escribirle al conductor
@Controller
public class MisReservasController {

    private final ContactoConductorService contactoConductorService;

    public MisReservasController(ContactoConductorService contactoConductorService) {
        this.contactoConductorService = contactoConductorService;
    }

    @GetMapping("/reservas")
    public String misReservas(Principal principal, Model model) {
        model.addAttribute("reservas", contactoConductorService.reservasDe(principal.getName()));
        return "mis-reservas";
    }
}
