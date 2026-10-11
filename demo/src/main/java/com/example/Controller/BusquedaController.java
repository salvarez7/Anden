package com.example.Controller;

import com.example.Model.Gama;
import com.example.Service.CatalogoVehiculosService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Para buscar los vehiculos según los filtros que ponga el usuario
@Controller
public class BusquedaController {

    private final CatalogoVehiculosService catalogoVehiculosService;

    public BusquedaController(CatalogoVehiculosService catalogoVehiculosService) {
        this.catalogoVehiculosService = catalogoVehiculosService;
    }

    @GetMapping("/vehiculos")
    public String buscar(@RequestParam(required = false) String gama,
                         @RequestParam(required = false) Integer asientos,
                         Model model) {
        model.addAttribute("gamas", Gama.values());
        model.addAttribute("gamaElegida", gama);
        model.addAttribute("asientosElegidos", asientos);
        model.addAttribute("resultados", catalogoVehiculosService.buscar(gama, asientos));
        return "vehiculos";
    }
}
