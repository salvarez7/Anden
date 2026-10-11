package com.example.Repository;

import com.example.Model.Vehiculo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehiculoRepository extends MongoRepository<Vehiculo, String> {

    List<Vehiculo> findByCorreoConductor(String correoConductor);

    List<Vehiculo> findByGama(String gama);
}
