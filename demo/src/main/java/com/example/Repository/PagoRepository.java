package com.example.Repository;

import com.example.Model.Pago;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends MongoRepository<Pago, String> {

    Optional<Pago> findByReservaCodigo(String reservaCodigo);

    List<Pago> findByCorreoTurista(String correoTurista);
}