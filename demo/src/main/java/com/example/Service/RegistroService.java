package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.Usuario;
import com.example.Repository.ConductorRepository;
import com.example.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final ConductorRepository conductorRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(UsuarioRepository usuarioRepository,
                           ConductorRepository conductorRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.conductorRepository = conductorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Revisa los requisitos del conductor (mayor de edad y licencia de 2 años)
    // y devuelve los mensajes de error para mostrarle al usuario.
    public ResultadoValidacion validarRequisitos(DatosRegistro datos) {
        List<String> errores = new ArrayList<>();

        // Mayoría de edad
        if (datos.edad() < 18) {
            errores.add("Debes ser mayor de edad (18 años o más) para registrarte como conductor.");
        }

        // Licencia con mínimo 2 años de expedición o renovación
        LocalDate fechaLicencia = datos.fechaExpedicionLicencia();
        if (fechaLicencia == null) {
            errores.add("Debes ingresar la fecha de expedición de tu licencia.");
        } else if (fechaLicencia.isAfter(LocalDate.now())) {
            errores.add("La fecha de expedición de la licencia no puede ser futura.");
        } else if (Period.between(fechaLicencia, LocalDate.now()).getYears() < 2) {
            errores.add("Tu licencia debe tener mínimo 2 años de expedición o renovación.");
        }

        return new ResultadoValidacion(errores.isEmpty(), errores);
    }

    // Guarda el usuario (y los datos extra si es conductor).
    // Si algo está mal lanza IllegalArgumentException con el mensaje a mostrar.
    public Usuario guardarUsuario(DatosRegistro datos) {
        String correo = datos.correo().trim().toLowerCase();

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        }

        boolean esConductor = "CONDUCTOR".equalsIgnoreCase(datos.tipoUsuario());

        if (esConductor) {
            ResultadoValidacion resultado = validarRequisitos(datos);
            if (!resultado.valido()) {
                throw new IllegalArgumentException(String.join(" ", resultado.errores()));
            }
        }

        // El passwordEncoder es el que definió Santiago en SecurityConfig
        Usuario usuario = new Usuario(
                datos.nombre().trim(),
                correo,
                passwordEncoder.encode(datos.password()),
                datos.tipoUsuario().toUpperCase());
        usuarioRepository.save(usuario);

        if (esConductor) {
            Conductor conductor = new Conductor(
                    correo,
                    datos.nombre().trim(),
                    datos.telefono(),
                    datos.edad(),
                    datos.numeroLicencia(),
                    datos.fechaExpedicionLicencia(),
                    datos.placaVehiculo());
            conductorRepository.save(conductor); // queda PENDIENTE_VERIFICACION
        }

        return usuario;
    }
}
