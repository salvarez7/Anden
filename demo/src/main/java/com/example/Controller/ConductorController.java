package com.example.Controller;

import com.example.Repository.ConductorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConductorController {

    private final ConductorRepository conductorRepository;

    public ConductorController(ConductorRepository conductorRepository) {
        this.conductorRepository = conductorRepository;
    }

    @GetMapping("/conductores")
    public String disponibles(Model model) {
        model.addAttribute("conductores", conductorRepository.findByEstado("APROBADO"));
        return "conductores";
    }
}