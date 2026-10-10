package com.example.Controller;

import com.example.Repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

// Perfil del turista se redirecciona a esta vista según el tipo despues del inicio de sesion
@Controller
public class PanelController {

    private final UsuarioRepository usuarioRepository;

    public PanelController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/turista")
    public String turista(Principal principal, Model model) {
        usuarioRepository.findByCorreo(principal.getName())
                .ifPresent(usuario -> model.addAttribute("usuario", usuario));
        return "turista";
    }
}
