package com.example.Controller;

import com.example.Repository.FotoVehiculoRepository;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;

@Controller
public class FotoController {

    private final FotoVehiculoRepository fotoRepository;

    public FotoController(FotoVehiculoRepository fotoRepository) {
        this.fotoRepository = fotoRepository;
    }

    @GetMapping("/fotos/{id}")
    public ResponseEntity<byte[]> ver(@PathVariable String id) {
        return fotoRepository.findById(id)
                .map(foto -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(foto.getTipoContenido()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePrivate())
                        .body(foto.getDatos()))
                .orElse(ResponseEntity.notFound().build());
    }
}