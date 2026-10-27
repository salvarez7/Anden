package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.VehiculoRepository;
import com.example.Service.AdminConductorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminConductorController {

    private final AdminConductorService adminService;
    private final ConductorRepository conductorRepository;
    private final VehiculoRepository vehiculoRepository;

    public AdminConductorController(AdminConductorService adminService,
                                    ConductorRepository conductorRepository,
                                    VehiculoRepository vehiculoRepository) {
        this.adminService = adminService;
        this.conductorRepository = conductorRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    // Lista de conductores; por defecto muestra los pendientes. "TODOS" muestra a todos
    @GetMapping("/admin/conductores")
    public String lista(@RequestParam(defaultValue = "PENDIENTE_VERIFICACION") String estado, Model model) {
        String filtro = "TODOS".equalsIgnoreCase(estado) ? null : estado;
        model.addAttribute("conductores", adminService.listar(filtro));
        model.addAttribute("estadoElegido", estado);
        return "admin-conductores";
    }

    // Datos del conductor, sus documentos y las fotos de su vehículo
    @GetMapping("/admin/conductores/{id}")
    public String detalle(@PathVariable String id, Model model) {
        Optional<Conductor> encontrado = conductorRepository.findById(id);
        if (encontrado.isEmpty()) {
            return "redirect:/admin/conductores";
        }
        Conductor conductor = encontrado.get();
        LocalDate hoy = LocalDate.now();

        List<Vehiculo> vehiculos = vehiculoRepository.findByCorreoConductor(conductor.getCorreo());
        boolean sinFotos = vehiculos.stream()
                .allMatch(v -> v.getFotos() == null || v.getFotos().isEmpty());

        model.addAttribute("conductor", conductor);
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("sinFotos", sinFotos);
        model.addAttribute("soatVigente", conductor.getFechaVencimientoSoat() != null
                && !conductor.getFechaVencimientoSoat().isBefore(hoy));
        model.addAttribute("tecnoVigente", conductor.getFechaVencimientoTecnomecanica() != null
                && !conductor.getFechaVencimientoTecnomecanica().isBefore(hoy));
        return "admin-conductor";
    }

    // El administrador aprueba o rechaza; el estado del conductor cambia según su decisión
    @PostMapping("/admin/conductores/{id}/decision")
    public String decidir(@PathVariable String id, @RequestParam String decision,
                          RedirectAttributes redirect) {
        try {
            Conductor conductor = adminService.decidir(id, decision);
            redirect.addFlashAttribute("mensaje",
                    AdminConductorService.APROBADO.equals(conductor.getEstado())
                            ? "Conductor aprobado." : "Conductor rechazado.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/conductores/" + id;
    }
}