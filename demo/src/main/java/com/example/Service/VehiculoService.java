package com.example.Service;

import com.example.Model.PeriodoOcupado;
import com.example.Model.Vehiculo;
import com.example.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    // true si el vehículo existe
    public boolean estaDisponible(String vehiculoId, LocalDate inicio, LocalDate fin) {
        return vehiculoRepository.findById(vehiculoId)
                .map(vehiculo -> estaLibre(vehiculo, inicio, fin))
                .orElse(false);
    }

  
    public List<Vehiculo> sugerirSimilares(String vehiculoId, LocalDate inicio, LocalDate fin) {
        return vehiculoRepository.findById(vehiculoId)
                .map(pedido -> filtrarSimilares(pedido, vehiculoRepository.findAll(), inicio, fin))
                .orElse(new ArrayList<>());
    }

   

    boolean estaLibre(Vehiculo vehiculo, LocalDate inicio, LocalDate fin) {
        if (!vehiculo.isDisponible()) {
            return false;
        }
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            return false;
        }
        for (PeriodoOcupado periodo : vehiculo.getPeriodosOcupados()) {
            
            boolean seCruzan = !inicio.isAfter(periodo.getFechaFin())
                    && !fin.isBefore(periodo.getFechaInicio());
            if (seCruzan) {
                return false;
            }
        }
        return true;
    }

    List<Vehiculo> filtrarSimilares(Vehiculo pedido, List<Vehiculo> todos,
                                    LocalDate inicio, LocalDate fin) {
        List<Vehiculo> mismaGama = new ArrayList<>();
        List<Vehiculo> otrasGamas = new ArrayList<>();

        for (Vehiculo v : todos) {
            boolean esElMismo = pedido.getId() != null && pedido.getId().equals(v.getId());
            if (esElMismo) {
                continue;
            }
            if (v.getNumeroAsientos() < pedido.getNumeroAsientos()) {
                continue; 
            }
            if (!estaLibre(v, inicio, fin)) {
                continue;
            }
            if (v.getGama() != null && v.getGama().equalsIgnoreCase(pedido.getGama())) {
                mismaGama.add(v);
            } else {
                otrasGamas.add(v);
            }
        }

      
        Comparator<Vehiculo> porAsientos = Comparator.comparingInt(Vehiculo::getNumeroAsientos);
        mismaGama.sort(porAsientos);
        otrasGamas.sort(porAsientos);

        mismaGama.addAll(otrasGamas);
        return mismaGama;
    }
}
