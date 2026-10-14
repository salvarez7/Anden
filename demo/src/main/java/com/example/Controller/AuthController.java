package com.example.Controller;

import com.example.Model.Usuario;
import com.example.Repository.ConductorRepository;
import com.example.Repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final ConductorRepository conductorRepository;

    public AuthController(UsuarioRepository usuarioRepository,
                          ConductorRepository conductorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.conductorRepository = conductorRepository;
    }

    // Página principal: le pasa al index la sesión y los datos del usuario (TURISTA o CONDUCTOR)
    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        boolean autenticado = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        String rol = "";
        if (autenticado) {
            String correo = authentication.getName();

            if (tieneRol(authentication, "ROLE_TURISTA")) {
                rol = "TURISTA";
            } else if (tieneRol(authentication, "ROLE_CONDUCTOR")) {
                rol = "CONDUCTOR";
            }

            Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
            String nombre = (usuario != null && usuario.getNombre() != null && !usuario.getNombre().isBlank())
                    ? usuario.getNombre()
                    : correo;

            model.addAttribute("correo", correo);
            model.addAttribute("nombre", nombre);
            model.addAttribute("inicial", nombre.substring(0, 1).toUpperCase());
            if (usuario != null) {
                model.addAttribute("usuario", usuario);
            }
            if ("CONDUCTOR".equals(rol)) {
                conductorRepository.findByCorreo(correo)
                        .ifPresent(conductor -> model.addAttribute("conductor", conductor));
            }
        }

        model.addAttribute("autenticado", autenticado);
        model.addAttribute("rol", rol);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/inicio")
    public String inicio(java.security.Principal principal, Model model) {
        model.addAttribute("correo", principal.getName());
        return "inicio";
    }

    @GetMapping("/reserva")
    public String reserva() {
        return "redirect:/vehiculos";
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }
}