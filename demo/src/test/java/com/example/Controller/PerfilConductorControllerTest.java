package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.FotoVehiculoRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.VehiculoRepository;
import com.example.demo.DemoApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.mongodb.uri=mongodb://localhost:27017/pruebas")
class PerfilConductorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ConductorRepository conductorRepository;

    @MockitoBean
    private VehiculoRepository vehiculoRepository;

    @MockitoBean
    private FotoVehiculoRepository fotoRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private Conductor juan;
    private Vehiculo duster;

    @BeforeEach
    void preparar() {
        juan = new Conductor();
        juan.setCorreo("juan@correo.com");
        juan.setNombre("Juan Pérez");
        juan.setEstado("APROBADO");
        duster = new Vehiculo("juan@correo.com", "Renault Duster", "estandar", 5);
        duster.setId("v1");
        duster.setFotos(new ArrayList<>(List.of("f1")));
        when(conductorRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(juan));
        when(vehiculoRepository.findByCorreoConductor("juan@correo.com")).thenReturn(List.of(duster));
    }

    @Test
    void elConductorVeSuFormularioYQueLeFaltaParaCompletarElPerfil() throws Exception {
        mvc.perform(get("/conductor/perfil").with(user("juan@correo.com").roles("CONDUCTOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Editar mi perfil")))
                .andExpect(content().string(containsString("Tu perfil está incompleto")))
                .andExpect(content().string(containsString("tus años de experiencia")));
    }

    @Test
    void unTuristaNoTienePerfilDeConductor() throws Exception {
        mvc.perform(get("/conductor/perfil").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void sinIniciarSesionLoMandaAlLogin() throws Exception {
        mvc.perform(get("/conductor/perfil"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void guardarActualizaElConductor() throws Exception {
        mvc.perform(multipart("/conductor/perfil").with(csrf())
                        .param("añosExperiencia", "7")
                        .param("zonas", "El Poblado, Aeropuerto")
                        .param("disponibilidadHoraria", "Todos los días de 5 a. m. a 9 p. m.")
                        .with(user("juan@correo.com").roles("CONDUCTOR")))
                .andExpect(redirectedUrl("/conductor/perfil?guardado"));

        assertEquals(7, juan.getAñosExperiencia());
        assertEquals(List.of("El Poblado", "Aeropuerto"), juan.getZonas());
        verify(conductorRepository).save(juan);
    }

    @Test
    void conDatosMalosMuestraLosErroresYNoGuarda() throws Exception {
        mvc.perform(multipart("/conductor/perfil").with(csrf())
                        .param("añosExperiencia", "")
                        .param("zonas", " , ")
                        .param("disponibilidadHoraria", "Solo de noche")
                        .with(user("juan@correo.com").roles("CONDUCTOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Escribe tus años de experiencia")))
                .andExpect(content().string(containsString("al menos una zona")))
                .andExpect(content().string(containsString("Solo de noche"))); // lo que escribió no se pierde

        verify(conductorRepository, never()).save(any());
    }

    // "Actualizar automáticamente el perfil público": la búsqueda lee los datos guardados
    @Test
    void laBusquedaMuestraLasPreferenciasGuardadas() throws Exception {
        juan.setAñosExperiencia(7);
        juan.setZonas(List.of("El Poblado", "Aeropuerto"));
        juan.setDisponibilidadHoraria("Todos los días");
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(1)).thenReturn(List.of(duster));

        mvc.perform(get("/vehiculos").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("7 años")))
                .andExpect(content().string(containsString("El Poblado, Aeropuerto")))
                .andExpect(content().string(containsString("Todos los días")));
    }
}
