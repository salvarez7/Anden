package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.Gama;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// filtración de vehículos según los filtros de gama y # de asientos. Cuando se registra el conductor se crea su vehiculo para poderlo buscar
@Service
public class CatalogoVehiculosService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    public CatalogoVehiculosService(VehiculoRepository vehiculoRepository,
                                    ConductorRepository conductorRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
    }

    // se guarda el carro que el conductor puso
    public Vehiculo registrarVehiculoDeConductor(String correoConductor, String marca, String modelo,
                                                 Gama gama, int asientos) {
        String nombreCarro = ((marca == null ? "" : marca.trim()) + " "
                + (modelo == null ? "" : modelo.trim())).trim();

        Vehiculo vehiculo = new Vehiculo(correoConductor.trim().toLowerCase(),
                nombreCarro, gama.getCodigo(), asientos);
        return vehiculoRepository.save(vehiculo);
    }

    // aca filtro por gama y asinetos
    public List<ResultadoBusqueda> buscar(String gama, Integer asientosMinimos) {
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
            Optional<Conductor> conductor = conductorRepository.findByCorreo(vehiculo.getCorreoConductor());
            if (conductor.isPresent() && "APROBADO".equals(conductor.get().getEstado())) {
                resultados.add(new ResultadoBusqueda(vehiculo, conductor.get()));
            }
        }

        resultados.sort(Comparator.comparingInt(r -> r.vehiculo().getNumeroAsientos()));
        return resultados;
    }
}
