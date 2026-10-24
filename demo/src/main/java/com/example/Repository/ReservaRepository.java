package com.example.Repository;

import com.example.Model.Reserva;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends MongoRepository<Reserva, String> {

    Optional<Reserva> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Reserva> findByCorreoTurista(String correoTurista);

  
    List<Reserva> findByEstadoAndFechaInicio(String estado, LocalDate fechaInicio);
}
