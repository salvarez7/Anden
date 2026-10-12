package com.example.Service;

import com.example.Model.FotoVehiculo;
import com.example.Repository.FotoVehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

// Valida y guarda las fotos del vehículo que sube el conductor al registrarse
@Service
public class FotoVehiculoService {

    static final int MAXIMO_FOTOS = 3;
    static final long MAXIMO_BYTES = 1500000; // 1,5 MB por foto

    private final FotoVehiculoRepository fotoRepository;

    public FotoVehiculoService(FotoVehiculoRepository fotoRepository) {
        this.fotoRepository = fotoRepository;
    }

    // Revisa las fotos ANTES de guardar nada. Devuelve los errores (vacía si todo está bien)
    public List<String> validar(List<MultipartFile> archivos) {
        List<String> errores = new ArrayList<>();
        List<MultipartFile> fotos = conArchivo(archivos);

        if (fotos.size() > MAXIMO_FOTOS) {
            errores.add("Puedes subir máximo " + MAXIMO_FOTOS + " fotos del vehículo.");
            return errores;
        }
        for (MultipartFile foto : fotos) {
            String nombre = foto.getOriginalFilename();
            if (foto.getSize() > MAXIMO_BYTES) {
                errores.add("La foto \"" + nombre + "\" pesa demasiado (máximo 1,5 MB).");
            } else if (tipoDe(foto) == null) {
                errores.add("La foto \"" + nombre + "\" debe ser JPG, PNG o WEBP.");
            }
        }
        return errores;
    }

    // Guarda las fotos y devuelve sus ids; esos ids son los que van en Vehiculo.fotos
    public List<String> guardar(List<MultipartFile> archivos) {
        List<String> ids = new ArrayList<>();
        for (MultipartFile foto : conArchivo(archivos)) {
            try {
                byte[] datos = foto.getBytes();
                String tipo = detectarTipo(datos);
                if (tipo == null) {
                    continue;
                }
                ids.add(fotoRepository.save(new FotoVehiculo(tipo, datos)).getId());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return ids;
    }

    // El navegador manda un archivo vacío cuando no se eligió ninguna foto
    private List<MultipartFile> conArchivo(List<MultipartFile> archivos) {
        List<MultipartFile> resultado = new ArrayList<>();
        if (archivos != null) {
            for (MultipartFile archivo : archivos) {
                if (archivo != null && !archivo.isEmpty()) {
                    resultado.add(archivo);
                }
            }
        }
        return resultado;
    }

    private String tipoDe(MultipartFile foto) {
        try {
            return detectarTipo(foto.getBytes());
        } catch (IOException e) {
            return null;
        }
    }

    // Mira los primeros bytes del archivo, no se fía de la extensión
    static String detectarTipo(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}