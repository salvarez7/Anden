package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.FotoVehiculoRepository;
import com.example.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Service
public class PerfilConductorService {

    static final int MAXIMO_AÑOS_EXPERIENCIA = 60;
    static final int MAXIMO_ZONAS = 10;
    static final int MAXIMO_CARACTERES_DISPONIBILIDAD = 120;
    static final int MAXIMO_FOTOS = 3;

    private final ConductorRepository conductorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final FotoVehiculoRepository fotoRepository;
    private final FotoVehiculoService fotoVehiculoService;

    public PerfilConductorService(ConductorRepository conductorRepository,
                                  VehiculoRepository vehiculoRepository,
                                  FotoVehiculoRepository fotoRepository,
                                  FotoVehiculoService fotoVehiculoService) {
        this.conductorRepository = conductorRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.fotoRepository = fotoRepository;
        this.fotoVehiculoService = fotoVehiculoService;
    }

    public Optional<Conductor> buscarConductor(String correo) {
        return conductorRepository.findByCorreo(correo);
    }


    public Optional<Vehiculo> buscarVehiculo(String correo) {
        return vehiculoRepository.findByCorreoConductor(correo).stream().findFirst();
    }


    public static List<String> separarZonas(String texto) {
        LinkedHashSet<String> zonas = new LinkedHashSet<>();
        if (texto != null) {
            for (String parte : texto.split(",")) {
                String zona = parte.trim();
                if (!zona.isEmpty()) {
                    zonas.add(zona);
                }
            }
        }
        return new ArrayList<>(zonas);
    }

    public List<String> validar(PreferenciasConductor datos, int fotosQueQuedan, List<MultipartFile> fotosNuevas) {
        List<String> errores = new ArrayList<>();

        Integer años = datos.añosExperiencia();
        if (años == null || años < 0 || años > MAXIMO_AÑOS_EXPERIENCIA) {
            errores.add("Escribe tus años de experiencia (entre 0 y " + MAXIMO_AÑOS_EXPERIENCIA + ").");
        }

        List<String> zonas = datos.zonas() == null ? List.of() : datos.zonas();
        if (zonas.isEmpty()) {
            errores.add("Escribe al menos una zona donde prestas el servicio.");
        } else if (zonas.size() > MAXIMO_ZONAS) {
            errores.add("Puedes poner máximo " + MAXIMO_ZONAS + " zonas.");
        }

        String horario = datos.disponibilidadHoraria() == null ? "" : datos.disponibilidadHoraria().trim();
        if (horario.isEmpty()) {
            errores.add("Cuéntales a los turistas en qué días y horas trabajas.");
        } else if (horario.length() > MAXIMO_CARACTERES_DISPONIBILIDAD) {
            errores.add("La disponibilidad no puede pasar de " + MAXIMO_CARACTERES_DISPONIBILIDAD + " caracteres.");
        }

        errores.addAll(fotoVehiculoService.validar(fotosNuevas));
        if (fotosQueQuedan + contarArchivos(fotosNuevas) > MAXIMO_FOTOS) {
            errores.add("Tu vehículo puede tener máximo " + MAXIMO_FOTOS + " fotos. Quita alguna antes de subir otra.");
        }
        return errores;
    }

    public Conductor actualizar(String correo, PreferenciasConductor datos,
                                List<String> fotosAQuitar, List<MultipartFile> fotosNuevas) {
        Conductor conductor = buscarConductor(correo)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos tu perfil de conductor."));
        Optional<Vehiculo> vehiculo = buscarVehiculo(correo);

        List<String> actuales = vehiculo.map(Vehiculo::getFotos).orElse(List.of());
        List<String> quitar = fotosAQuitar == null ? List.of()
                : fotosAQuitar.stream().filter(actuales::contains).toList();
        int quedan = actuales.size() - quitar.size();

        List<String> errores = validar(datos, quedan, fotosNuevas);
        if (vehiculo.isEmpty() && contarArchivos(fotosNuevas) > 0) {
            errores.add("No encontramos tu vehículo para guardarle las fotos.");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }

        conductor.setAñosExperiencia(datos.añosExperiencia());
        conductor.setZonas(new ArrayList<>(datos.zonas()));
        conductor.setDisponibilidadHoraria(datos.disponibilidadHoraria().trim());
        conductorRepository.save(conductor);

        if (vehiculo.isPresent()) {
            Vehiculo v = vehiculo.get();
            List<String> fotos = new ArrayList<>(v.getFotos());
            fotos.removeAll(quitar);
            fotos.addAll(fotoVehiculoService.guardar(fotosNuevas));
            v.setFotos(fotos);
            vehiculoRepository.save(v);
            quitar.forEach(fotoRepository::deleteById);
        }
        return conductor;
    }

    public List<String> loQueFalta(Conductor conductor, Vehiculo vehiculo) {
        List<String> faltan = new ArrayList<>();
        if (conductor.getAñosExperiencia() == null) {
            faltan.add("tus años de experiencia");
        }
        if (conductor.getZonas() == null || conductor.getZonas().isEmpty()) {
            faltan.add("las zonas donde trabajas");
        }
        if (conductor.getDisponibilidadHoraria() == null || conductor.getDisponibilidadHoraria().isBlank()) {
            faltan.add("tu disponibilidad horaria");
        }
        if (vehiculo == null || vehiculo.getFotos().isEmpty()) {
            faltan.add("al menos una foto de tu vehículo");
        }
        return faltan;
    }

    private static int contarArchivos(List<MultipartFile> archivos) {
        if (archivos == null) {
            return 0;
        }
        int total = 0;
        for (MultipartFile archivo : archivos) {
            if (archivo != null && !archivo.isEmpty()) {
                total++;
            }
        }
        return total;
    }
}
