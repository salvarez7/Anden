package com.example.Repository;

import com.example.Model.FotoVehiculo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FotoVehiculoRepository extends MongoRepository<FotoVehiculo, String> {
}