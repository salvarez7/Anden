package com.example.Repository;

import com.example.Model.Conductor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConductorRepository
        extends MongoRepository<Conductor, String> {

    Optional<Conductor> findByCorreo(String correo);

    // Lo va a usar el administrador (Sprint 6) para ver los pendientes
    List<Conductor> findByEstado(String estado);
}
