package com.example.Service;

import com.example.Model.Conductor;
import com.example.Repository.ConductorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// El administrador revisa a los conductores y decide si los aprueba o los rechaza
@Service
public class AdminConductorService {

    public static final String APROBADO = "APROBADO";
    public static final String RECHAZADO = "RECHAZADO";

    private final ConductorRepository conductorRepository;

    public AdminConductorService(ConductorRepository conductorRepository) {
        this.conductorRepository = conductorRepository;
    }

    // Lista por estado; si el estado viene vacío devuelve todos
    public List<Conductor> listar(String estado) {
        if (estado == null || estado.isBlank()) {
            return conductorRepository.findAll();
        }
        return conductorRepository.findByEstado(estado);
    }

    // Cambia el estado según la decisión del administrador: "APROBAR" o "RECHAZAR"
    public Conductor decidir(String conductorId, String decision) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos a ese conductor."));

        if ("APROBAR".equalsIgnoreCase(decision)) {
            conductor.setEstado(APROBADO);
        } else if ("RECHAZAR".equalsIgnoreCase(decision)) {
            conductor.setEstado(RECHAZADO);
        } else {
            throw new IllegalArgumentException("La decisión no es válida.");
        }
        return conductorRepository.save(conductor);
    }
}