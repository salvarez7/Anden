package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Model.Vehiculo;
import com.example.Service.PerfilConductorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Para que el conductor pomga las preferencias
@Controller
public class PerfilConductorController {

    private final PerfilConductorService perfilConductorService;

    public PerfilConductorController(PerfilConductorService perfilConductorService) {
        this.perfilConductorService = perfilConductorService;
    }

    @GetMapping("/conductor/perfil")
    public String mostrar(Principal principal, Model model) {
        Optional<Conductor> conductor = perfilConductorService.buscarConductor(principal.getName());
        if (conductor.isEmpty()) {
            return "redirect:/";
        }
        return preparar(conductor.get(), FormularioPerfilConductor.desde(conductor.get()), List.of(), model);
    }

    @PostMapping("/conductor/perfil")
    public String guardar(@ModelAttribute("formulario") FormularioPerfilConductor formulario,
                          @RequestParam(value = "fotos", required = false) List<MultipartFile> fotos,
                          Principal principal, Model model) {
        Optional<Conductor> conductor = perfilConductorService.buscarConductor(principal.getName());
        if (conductor.isEmpty()) {
            return "redirect:/";
        }
        try {
            perfilConductorService.actualizar(principal.getName(), formulario.aPreferencias(),
                    formulario.getFotosAQuitar(), fotos);
        } catch (IllegalArgumentException e) {

            List<String> errores = Arrays.asList(e.getMessage().split("\n"));
            return preparar(conductor.get(), formulario, errores, model);
        }
        return "redirect:/conductor/perfil?guardado";
    }

    private String preparar(Conductor conductor, FormularioPerfilConductor formulario,
                            List<String> errores, Model model) {
        Vehiculo vehiculo = perfilConductorService.buscarVehiculo(conductor.getCorreo()).orElse(null);
        model.addAttribute("conductor", conductor);
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("formulario", formulario);
        model.addAttribute("errores", errores);
        model.addAttribute("faltan", perfilConductorService.loQueFalta(conductor, vehiculo));
        return "perfil-conductor";
    }
}
