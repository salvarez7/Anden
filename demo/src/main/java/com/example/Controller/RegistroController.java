package com.example.Controller;

import com.example.Service.DatosRegistro;
import com.example.Service.RegistroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

// registro de turista y registro de conductor
@Controller
public class RegistroController {

    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    // Muestra el formulario. de registro a turista o si es conductor a conductor
    @GetMapping("/registro")
    public String mostrarFormulario(@RequestParam(defaultValue = "TURISTA") String tipo, Model model) {
        FormularioRegistro formulario = new FormularioRegistro();
        formulario.setTipoUsuario("CONDUCTOR".equalsIgnoreCase(tipo) ? "CONDUCTOR" : "TURISTA");
        model.addAttribute("formulario", formulario);
        return "registro";
    }

    // Recibe el formulario
    @PostMapping("/registro")
    public String registrar(@ModelAttribute("formulario") FormularioRegistro formulario, Model model) {
        DatosRegistro datos = formulario.aDatosRegistro();

        //  Validaciones (requisitos si es conductor)
        List<String> errores = new ArrayList<>(registroService.validarDatosBasicos(datos).errores());
        if (formulario.esConductor()) {
            errores.addAll(registroService.validarRequisitos(datos).errores());
        }

        // 2. si esta bien lo guarda en la BD y si algo avisa si el correo ya esta registrado
        if (errores.isEmpty()) {
            try {
                registroService.guardarUsuario(datos);
            } catch (IllegalArgumentException e) {
                errores.add(e.getMessage());
            }
        }

        if (!errores.isEmpty()) {
            // Se muestra el formulario con los mensajes de error
            formulario.setPassword(null);
            model.addAttribute("errores", errores);
            return "registro";
        }

        //  muestra cuenta creada
        return "redirect:/login?registrado";
    }
}
