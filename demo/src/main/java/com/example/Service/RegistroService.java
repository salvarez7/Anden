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
import java.util.regex.Pattern;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final ConductorRepository conductorRepository;
    private final PasswordEncoder passwordEncoder;

    // texto@texto.dominio (sin espacios)
    private static final Pattern FORMATO_CORREO =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    // Celular colombiano: 10 dígitos que empiezan por 3 (ej. 3001234567)
    private static final Pattern FORMATO_TELEFONO = Pattern.compile("^3\\d{9}$");

    public RegistroService(UsuarioRepository usuarioRepository,
                           ConductorRepository conductorRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.conductorRepository = conductorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Quita espacios, guiones, paréntesis y el +57 para dejar solo los 10 dígitos.
    // Ej: "+57 300 123-4567" -> "3001234567"
    public static String normalizarTelefono(String telefono) {
        if (telefono == null) {
            return "";
        }
        String soloDigitos = telefono.replaceAll("[\\s()\\-+]", "");
        if (soloDigitos.startsWith("57") && soloDigitos.length() == 12) {
            soloDigitos = soloDigitos.substring(2);
        }
        return soloDigitos;
    }

    // Revisa los datos que piden tanto turista como conductor
    // (H1: nombre, correo, teléfono y contraseña con formato correcto).
    public ResultadoValidacion validarDatosBasicos(DatosRegistro datos) {
        List<String> errores = new ArrayList<>();

        if (datos.nombre() == null || datos.nombre().isBlank()) {
            errores.add("Debes ingresar tu nombre.");
        }

        if (datos.correo() == null || !FORMATO_CORREO.matcher(datos.correo().trim()).matches()) {
            errores.add("El correo no tiene un formato válido (ej. nombre@correo.com).");
        }

        if (!FORMATO_TELEFONO.matcher(normalizarTelefono(datos.telefono())).matches()) {
            errores.add("El teléfono debe ser un celular de 10 dígitos que empiece por 3 (ej. 300 123 4567).");
        }

        if (datos.password() == null || datos.password().length() < 6) {
            errores.add("La contraseña debe tener mínimo 6 caracteres.");
        }

        // Solo se puede crear cuenta de turista o de conductor (nunca de administrador)
        if (!"TURISTA".equalsIgnoreCase(datos.tipoUsuario())
                && !"CONDUCTOR".equalsIgnoreCase(datos.tipoUsuario())) {
            errores.add("Tipo de usuario no válido.");
        }

        return new ResultadoValidacion(errores.isEmpty(), errores);
    }

    // Revisa los requisitos del conductor (mayor de edad, licencia de 2 años,
    // datos del vehículo, SOAT y tecnomecánica) y devuelve los mensajes de error para mostrarle al usuario.
    public ResultadoValidacion validarRequisitos(DatosRegistro datos) {
        List<String> errores = new ArrayList<>();

        // Mayoría de edad
        if (datos.edad() < 18) {
            errores.add("Debes ser mayor de edad (18 años o más) para registrarte como conductor.");
        }

        if (datos.numeroLicencia() == null || datos.numeroLicencia().isBlank()) {
            errores.add("Debes ingresar el número de tu licencia de conducción.");
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

        // Información del vehículo
        if (datos.placaVehiculo() == null || datos.placaVehiculo().isBlank()) {
            errores.add("Debes ingresar la placa del vehículo.");
        }
        if (datos.asientosVehiculo() < 1) {
            errores.add("Debes indicar cuántos pasajeros caben en el vehículo.");
        }

        // SOAT y tecnomecánica al día (la fecha de vencimiento no puede haber pasado)
        if (datos.fechaVencimientoSoat() == null
                || datos.fechaVencimientoSoat().isBefore(LocalDate.now())) {
            errores.add("El SOAT del vehículo debe estar vigente.");
        }
        if (datos.fechaVencimientoTecnomecanica() == null
                || datos.fechaVencimientoTecnomecanica().isBefore(LocalDate.now())) {
            errores.add("La revisión tecnomecánica del vehículo debe estar vigente.");
        }

        return new ResultadoValidacion(errores.isEmpty(), errores);
    }

    // Guarda el usuario (y los datos extra si es conductor).
    // Si algo está mal lanza IllegalArgumentException con el mensaje a mostrar.
    public Usuario guardarUsuario(DatosRegistro datos) {
        ResultadoValidacion basicos = validarDatosBasicos(datos);
        if (!basicos.valido()) {
            throw new IllegalArgumentException(String.join(" ", basicos.errores()));
        }

        String correo = datos.correo().trim().toLowerCase();
        String telefono = normalizarTelefono(datos.telefono());

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
                telefono,
                passwordEncoder.encode(datos.password()),
                datos.tipoUsuario().toUpperCase());
        usuarioRepository.save(usuario);

        if (esConductor) {
            Conductor conductor = new Conductor(
                    correo,
                    datos.nombre().trim(),
                    telefono,
                    datos.edad(),
                    datos.numeroLicencia(),
                    datos.fechaExpedicionLicencia(),
                    datos.placaVehiculo().trim().toUpperCase(),
                    datos.marcaVehiculo(),
                    datos.modeloVehiculo(),
                    datos.asientosVehiculo(),
                    datos.fechaVencimientoSoat(),
                    datos.fechaVencimientoTecnomecanica());
            conductorRepository.save(conductor); // queda PENDIENTE_VERIFICACION
        }

        return usuario;
    }
}
