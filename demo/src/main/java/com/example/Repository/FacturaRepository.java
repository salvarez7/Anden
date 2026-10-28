package com.example.Repository;

import com.example.Model.Factura;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FacturaRepository extends MongoRepository<Factura, String> {

    Optional<Factura> findByCodigoReserva(String codigoReserva);
}
