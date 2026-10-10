package com.example.Repository;

import com.example.Model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UsuarioRepository
        extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByCorreo(String correo);
}
