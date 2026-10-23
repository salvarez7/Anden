package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.FotoVehiculo;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.FotoVehiculoRepository;
import com.example.Repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PerfilConductorServiceTest {

    private final ConductorRepository conductorRepository = mock(ConductorRepository.class);
    private final VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
    private final FotoVehiculoRepository fotoRepository = mock(FotoVehiculoRepository.class);
    private final PerfilConductorService servicio = new PerfilConductorService(conductorRepository,
            vehiculoRepository, fotoRepository, new FotoVehiculoService(fotoRepository));

    private Conductor juan;
    private Vehiculo duster;

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    private MultipartFile foto(String nombre) {
        return new MockMultipartFile("fotos", nombre, "image/png", PNG);
    }

    private PreferenciasConductor buenas() {
        return new PreferenciasConductor(5, List.of("El Poblado", "Aeropuerto"), "Lunes a sábado, 6 a. m. a 8 p. m.");
    }

    @BeforeEach
    void preparar() {
        juan = new Conductor();
        juan.setCorreo("juan@x.com");
        juan.setNombre("Juan");
        duster = new Vehiculo("juan@x.com", "Renault Duster", "estandar", 5);
        duster.setFotos(new ArrayList<>(List.of("f1", "f2")));
        when(conductorRepository.findByCorreo("juan@x.com")).thenReturn(Optional.of(juan));
        when(vehiculoRepository.findByCorreoConductor("juan@x.com")).thenReturn(List.of(duster));
        when(fotoRepository.save(any(FotoVehiculo.class))).thenAnswer(i -> {
            FotoVehiculo f = i.getArgument(0);
            f.setId("nueva");
            return f;
        });
    }

    @Test
    void separarZonasQuitaEspaciosVaciosYRepetidas() {
        assertEquals(List.of("El Poblado", "Laureles", "Aeropuerto"),
                PerfilConductorService.separarZonas(" El Poblado, Laureles ,, Aeropuerto, Laureles"));
        assertTrue(PerfilConductorService.separarZonas(null).isEmpty());
    }

    @Test
    void preferenciasBienNoTienenErrores() {
        assertTrue(servicio.validar(buenas(), 2, List.of()).isEmpty());
    }

    @Test
    void preferenciasMalasDanUnErrorPorCampo() {
        List<String> errores = servicio.validar(new PreferenciasConductor(-1, List.of(), "  "), 1, List.of());

        assertEquals(3, errores.size());
        assertTrue(errores.get(0).contains("años de experiencia"));
        assertTrue(errores.get(1).contains("al menos una zona"));
        assertTrue(errores.get(2).contains("días y horas"));
    }

    @Test
    void noDejaPasarDeTresFotos() {
        List<String> errores = servicio.validar(buenas(), 2, List.of(foto("a.png"), foto("b.png")));

        assertTrue(errores.stream().anyMatch(e -> e.contains("máximo 3 fotos")));
    }

    @Test
    void actualizarGuardaLasPreferenciasDelConductor() {
        servicio.actualizar("juan@x.com", buenas(), List.of(), List.of());

        assertEquals(5, juan.getAñosExperiencia());
        assertEquals(List.of("El Poblado", "Aeropuerto"), juan.getZonas());
        assertEquals("Lunes a sábado, 6 a. m. a 8 p. m.", juan.getDisponibilidadHoraria());
        verify(conductorRepository).save(juan);
    }

    @Test
    void actualizarQuitaYAgregaFotosDelVehiculo() {
        servicio.actualizar("juan@x.com", buenas(), List.of("f1"), List.of(foto("nueva.png")));

        assertEquals(List.of("f2", "nueva"), duster.getFotos());
        verify(vehiculoRepository).save(duster);
        verify(fotoRepository).deleteById("f1");
    }

    @Test
    void noSePuedeQuitarUnaFotoDeOtroVehiculo() {
        servicio.actualizar("juan@x.com", buenas(), List.of("foto-de-otro"), List.of());

        assertEquals(List.of("f1", "f2"), duster.getFotos());
        verify(fotoRepository, never()).deleteById("foto-de-otro");
    }

    @Test
    void conErroresNoGuardaNada() {
        assertThrows(IllegalArgumentException.class, () ->
                servicio.actualizar("juan@x.com", new PreferenciasConductor(null, List.of(), ""), List.of(), List.of()));

        verify(conductorRepository, never()).save(any());
        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    void perfilSinFotosEstaIncompleto() {
        juan.setAñosExperiencia(5);
        juan.setZonas(List.of("Laureles"));
        juan.setDisponibilidadHoraria("Fines de semana");
        duster.setFotos(new ArrayList<>());

        assertEquals(List.of("al menos una foto de tu vehículo"), servicio.loQueFalta(juan, duster));
    }

    @Test
    void perfilConTodoEstaCompleto() {
        juan.setAñosExperiencia(0); // 0 años también es un dato válido
        juan.setZonas(List.of("Laureles"));
        juan.setDisponibilidadHoraria("Fines de semana");

        assertTrue(servicio.loQueFalta(juan, duster).isEmpty());
    }
}
