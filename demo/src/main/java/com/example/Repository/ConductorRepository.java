package com.example.Repository;

import com.example.Model.Conductor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConductorRepository
        extends MongoRepository<Conductor, String> {

    Optional<Conductor> findByCorreo(String correo);

    
    List<Conductor> findByEstado(String estado);
}
