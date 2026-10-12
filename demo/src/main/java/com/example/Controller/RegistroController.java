package com.example.Controller;

import com.example.Model.Gama;
import com.example.Service.CatalogoVehiculosService;
import com.example.Service.DatosRegistro;
import com.example.Service.FotoVehiculoService;
import com.example.Service.RegistroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

// registro de turista y registro de conductor
@Controller
public class RegistroController {

    private final RegistroService registroService;
    private final CatalogoVehiculosService catalogoVehiculosService;
    private final FotoVehiculoService fotoVehiculoService;

    public RegistroController(RegistroService registroService,
                              CatalogoVehiculosService catalogoVehiculosService,
                              FotoVehiculoService fotoVehiculoService) {
        this.registroService = registroService;
        this.catalogoVehiculosService = catalogoVehiculosService;
        this.fotoVehiculoService = fotoVehiculoService;
    }

    // Muestra el formulario. de registro a turista o si es conductor a conductor
    @GetMapping("/registro")
    public String mostrarFormulario(@RequestParam(defaultValue = "TURISTA") String tipo, Model model) {
        FormularioRegistro formulario = new FormularioRegistro();
        formulario.setTipoUsuario("CONDUCTOR".equalsIgnoreCase(tipo) ? "CONDUCTOR" : "TURISTA");
        model.addAttribute("formulario", formulario);
        model.addAttribute("gamas", Gama.values()); // opciones de gama (Sprint 2)
        return "registro";
    }

    // para recibir lo que se escribio en el formulario (y las fotos del vehículo si es conductor)
    @PostMapping("/registro")
    public String registrar(@ModelAttribute("formulario") FormularioRegistro formulario,
                            @RequestParam(value = "fotos", required = false) List<MultipartFile> fotos,
                            Model model) {
        DatosRegistro datos = formulario.aDatosRegistro();

        //  Valida los requisitos si es conductor
        List<String> errores = new ArrayList<>(registroService.validarDatosBasicos(datos).errores());
        Gama gama = Gama.desde(formulario.getGamaVehiculo());
        if (formulario.esConductor()) {
            errores.addAll(registroService.validarRequisitos(datos).errores());
            if (gama == null) {
                errores.add("Elige la gama de tu vehículo.");
            }
            errores.addAll(fotoVehiculoService.validar(fotos));
        }

        //si todo esta bien lo guarda en la BD
        if (errores.isEmpty()) {
            try {
                registroService.guardarUsuario(datos);
                // guardamos el conductor con su vehículo (y sus fotos) en la colección vehículos
                if (formulario.esConductor()) {
                    List<String> idsFotos = fotoVehiculoService.guardar(fotos);
                    catalogoVehiculosService.registrarVehiculoDeConductor(datos.correo(),
                            datos.marcaVehiculo(), datos.modeloVehiculo(), gama,
                            datos.asientosVehiculo(), idsFotos);
                }
            } catch (IllegalArgumentException e) {
                errores.add(e.getMessage());
            }
        }

        if (!errores.isEmpty()) {
            // Se muestra el formulario con los mensajes de error
            formulario.setPassword(null);
            model.addAttribute("errores", errores);
            model.addAttribute("gamas", Gama.values());
            return "registro";
        }

        //  muestra cuenta cresda
        return "redirect:/login?registrado";
    }
}