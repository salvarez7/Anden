package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Model.PeriodoOcupado;
import com.example.Model.Usuario;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.mongodb.uri=mongodb://localhost:27017/pruebas")
class ReservaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VehiculoRepository vehiculoRepository;

    @MockitoBean
    private ConductorRepository conductorRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private Vehiculo duster;

    @BeforeEach
    void preparar() {
        duster = new Vehiculo("juan@correo.com", "Renault Duster", "estandar", 5);
        duster.setId("v1");
        Conductor juan = new Conductor();
        juan.setCorreo("juan@correo.com");
        juan.setNombre("Juan Pérez");
        juan.setTelefono("3001234567");
        juan.setEstado("APROBADO");
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(duster));
        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(juan));
        when(usuarioRepository.findByCorreo("ana@correo.com"))
                .thenReturn(Optional.of(new Usuario("Ana", "ana@correo.com", "3109876543", "x", "TURISTA")));
    }

    @Test
    void elBotonAbreElFormularioConElVehiculo() throws Exception {
        mvc.perform(get("/reservas/nueva").param("vehiculoId", "v1")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Renault Duster")))
                .andExpect(content().string(containsString("Juan Pérez")))
                .andExpect(content().string(not(containsString("wa.me"))));
    }

    @Test
    void vehiculoQueNoExisteVuelveALaBusqueda() throws Exception {
        when(vehiculoRepository.findById("nada")).thenReturn(Optional.empty());
        mvc.perform(get("/reservas/nueva").param("vehiculoId", "nada")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/vehiculos"));
    }



    @Test
    void fechaFinAnteriorAInicioMuestraError() throws Exception {
        LocalDate inicio = LocalDate.now().plusDays(10);
        mvc.perform(post("/reservas/nueva").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.minusDays(2).toString())
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("posterior a la de inicio")))
                .andExpect(content().string(not(containsString("wa.me"))));
    }

    @Test
    void fechaEnElPasadoMuestraError() throws Exception {
        LocalDate ayer = LocalDate.now().minusDays(1);
        mvc.perform(post("/reservas/nueva").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", ayer.toString())
                        .param("fechaFin", LocalDate.now().plusDays(2).toString())
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(content().string(containsString("no puede estar en el pasado")));
    }

    @Test
    void vehiculoOcupadoMuestraErrorYNoDaEnlace() throws Exception {
        LocalDate inicio = LocalDate.now().plusDays(10);
        duster.getPeriodosOcupados().add(new PeriodoOcupado(inicio.plusDays(1), inicio.plusDays(5)));
        mvc.perform(post("/reservas/nueva").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(3).toString())
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(content().string(containsString("no está disponible en esas fechas")))
                .andExpect(content().string(not(containsString("wa.me"))));
    }

    @Test
    void sinIniciarSesionLoMandaAlLogin() throws Exception {
        mvc.perform(get("/reservas/nueva").param("vehiculoId", "v1"))
                .andExpect(status().is3xxRedirection());
    }
    @Test
    void conFechasLibresNoMuestraElWhatsAppHastaConfirmar() throws Exception {
        // RNF02: el número del conductor solo se ve con la reserva confirmada (en el ticket)
        LocalDate inicio = LocalDate.now().plusDays(10);
        mvc.perform(post("/reservas/nueva").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(3).toString())
                        .param("pasajeros", "3")
                        .param("trayectos", "Aeropuerto a hotel")
                        .param("numeroTrayectos", "2")
                        .param("horasPorDia", "6")
                        .param("trayectosFuera", "0")
                        .param("horaInicio", "9")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fechas disponibles")))
                .andExpect(content().string(containsString("Confirmar reserva")))
                .andExpect(content().string(not(containsString("wa.me"))))
                .andExpect(content().string(not(containsString("3001234567"))));
    }

    @Test
    void masPasajerosQueLosPuestosDelVehiculoMuestraError() throws Exception {
        LocalDate inicio = LocalDate.now().plusDays(10);
        mvc.perform(post("/reservas/nueva").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(3).toString())
                        .param("pasajeros", "9")
                        .param("trayectos", "Aeropuerto a hotel")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(content().string(containsString("capacidad para 5 pasajeros")))
                .andExpect(content().string(not(containsString("wa.me"))));
    }

    @Test
    void sinTrayectosMuestraError() throws Exception {
        LocalDate inicio = LocalDate.now().plusDays(10);
        mvc.perform(post("/reservas/nueva").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(3).toString())
                        .param("pasajeros", "2")
                        .param("trayectos", "   ")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(content().string(containsString("trayectos que quieres hacer")))
                .andExpect(content().string(not(containsString("wa.me"))));
    }
}
