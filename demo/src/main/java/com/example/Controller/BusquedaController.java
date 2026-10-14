package com.example.Controller;

import com.example.Model.Gama;
import com.example.Service.CatalogoVehiculosService;
import com.example.Service.ResultadoBusqueda;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
                         // Sprint 3: fechas de la reserva (el navegador las manda como 2026-10-20)
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         Model model) {
        model.addAttribute("gamas", Gama.values());
        model.addAttribute("gamaElegida", gama);
        model.addAttribute("asientosElegidos", asientos);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("hoy", LocalDate.now());

        // Si las fechas están mal no se busca: se muestran los errores
        List<String> erroresFechas = catalogoVehiculosService.validarFechas(desde, hasta);
        model.addAttribute("erroresFechas", erroresFechas);

        List<ResultadoBusqueda> resultados = new ArrayList<>();
        if (erroresFechas.isEmpty()) {
            resultados = catalogoVehiculosService.buscar(gama, asientos, desde, hasta);
        }
        model.addAttribute("resultados", resultados);
        return "vehiculos";
    }
}
