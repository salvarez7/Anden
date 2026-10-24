package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.Gama;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// filtración de vehículos según los filtros de gama y # de asientos. Cuando se registra el conductor se crea su vehiculo para poderlo buscar
@Service
public class CatalogoVehiculosService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final VehiculoService vehiculoService;

    public CatalogoVehiculosService(VehiculoRepository vehiculoRepository,
                                    ConductorRepository conductorRepository,
                                    VehiculoService vehiculoService) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.vehiculoService = vehiculoService;
    }

    // se guarda el carro que el conductor puso
    public Vehiculo registrarVehiculoDeConductor(String correoConductor, String marca, String modelo,
                                                 Gama gama, int asientos) {
        return registrarVehiculoDeConductor(correoConductor, marca, modelo, gama, asientos, new ArrayList<>());
    }

    // lo mismo, pero con los ids de las fotos que subió el conductor
    public Vehiculo registrarVehiculoDeConductor(String correoConductor, String marca, String modelo,
                                                 Gama gama, int asientos, List<String> fotos) {
        String nombreCarro = ((marca == null ? "" : marca.trim()) + " "
                + (modelo == null ? "" : modelo.trim())).trim();

        Vehiculo vehiculo = new Vehiculo(correoConductor.trim().toLowerCase(),
                nombreCarro, gama.getCodigo(), asientos);
        vehiculo.setFotos(new ArrayList<>(fotos));
        return vehiculoRepository.save(vehiculo);
    }

    // aca filtro por gama y asinetos
    public List<ResultadoBusqueda> buscar(String gama, Integer asientosMinimos) {
        return buscar(gama, asientosMinimos, null, null);
    }


    public List<ResultadoBusqueda> conductoresDisponibles(LocalDate inicio, LocalDate fin) {
        return buscar(null, null, inicio, fin);
    }

    // Valida fechas
    public List<String> validarFechas(LocalDate inicio, LocalDate fin) {
        List<String> errores = new ArrayList<>();
        if (inicio == null && fin == null) {
            return errores; // no eligió fechas: se busca sin ellas
        }
        if (inicio == null || fin == null) {
            errores.add("Elige las dos fechas: desde y hasta.");
        } else if (inicio.isBefore(LocalDate.now())) {
            errores.add("La fecha de inicio no puede estar en el pasado.");
        } else if (fin.isBefore(inicio)) {
            errores.add("La fecha final debe ser igual o posterior a la de inicio.");
        }
        return errores;
    }

    // Filtrado
    public List<ResultadoBusqueda> buscar(String gama, Integer asientosMinimos,
                                          LocalDate inicio, LocalDate fin) {
        boolean conFechas = inicio != null && fin != null;
        int minimo = (asientosMinimos == null || asientosMinimos < 1) ? 1 : asientosMinimos;
        Gama gamaElegida = Gama.desde(gama);


        List<Vehiculo> vehiculos;
        if (gamaElegida == null) {
            vehiculos = vehiculoRepository.findByNumeroAsientosGreaterThanEqual(minimo);
        } else {
            vehiculos = vehiculoRepository.findByGamaAndNumeroAsientosGreaterThanEqual(
                    gamaElegida.getCodigo(), minimo);
        }

        List<ResultadoBusqueda> resultados = new ArrayList<>();
        for (Vehiculo vehiculo : vehiculos) {
            if (!vehiculo.isDisponible()) {
                continue;
            }
            if (conFechas && !vehiculoService.estaLibre(vehiculo, inicio, fin)) {
                continue;
            }
            Optional<Conductor> conductor = conductorRepository.findByCorreo(vehiculo.getCorreoConductor());
            if (conductor.isPresent() && "APROBADO".equals(conductor.get().getEstado())) {
                resultados.add(new ResultadoBusqueda(vehiculo, conductor.get()));
            }
        }

        resultados.sort(Comparator.comparingInt(r -> r.vehiculo().getNumeroAsientos()));
        return resultados;
    }
}
