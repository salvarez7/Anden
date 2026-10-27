package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Model.Reserva;
import com.example.Model.Usuario;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.ReservaRepository;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
class ServiciosConductorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VehiculoRepository vehiculoRepository;

    @MockitoBean
    private ConductorRepository conductorRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private ReservaRepository reservaRepository;

    private Reserva reserva;

    @BeforeEach
    void preparar() {
        Vehiculo duster = new Vehiculo("juan@correo.com", "Renault Duster", "estandar", 5);
        duster.setId("v1");
        Conductor juan = new Conductor();
        juan.setCorreo("juan@correo.com");
        juan.setNombre("Juan Pérez");
        juan.setTelefono("3001234567");
        juan.setEstado("APROBADO");
        
        reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                LocalDate.now().minusDays(2), LocalDate.now(), 3, "Aeropuerto a hotel");

        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(duster));
        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(juan));
        when(usuarioRepository.findByCorreo("ana@correo.com"))
                .thenReturn(Optional.of(new Usuario("Ana", "ana@correo.com", "3109876543", "x", "TURISTA")));
        when(reservaRepository.findByCorreoConductor("juan@correo.com")).thenReturn(List.of(reserva));
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(reserva));
    }

    @Test
    void elConductorVeSusServiciosYElFormularioSiYaTerminaron() throws Exception {
        mvc.perform(get("/conductor/servicios").with(user("juan@correo.com").roles("CONDUCTOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AND-1234")))
                .andExpect(content().string(containsString("Registrar valor final")));
    }

    @Test
    void siElServicioNoHaTerminadoNoHayFormulario() throws Exception {
        reserva.setFechaInicio(LocalDate.now().plusDays(5));
        reserva.setFechaFin(LocalDate.now().plusDays(8));

        mvc.perform(get("/conductor/servicios").with(user("juan@correo.com").roles("CONDUCTOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Podrás registrar el valor final desde el")))
                .andExpect(content().string(not(containsString("Registrar valor final"))));
    }

    @Test
    void registrarElValorFinalGuardaYVuelveALaLista() throws Exception {
        mvc.perform(post("/conductor/servicios/AND-1234/valor-final").with(csrf())
                        .param("kilometros", "100")
                        .param("horas", "8")
                        .with(user("juan@correo.com").roles("CONDUCTOR")))
                .andExpect(redirectedUrl("/conductor/servicios?guardado=AND-1234"));

        verify(reservaRepository).save(reserva);
        // Estándar: 8 h * 12.000 + 100 km * 1.450
        assertEquals(241_000L, reserva.getValorFinal());
    }

    @Test
    void sinKilometrosMuestraElErrorYNoGuarda() throws Exception {
        mvc.perform(post("/conductor/servicios/AND-1234/valor-final").with(csrf())
                        .param("horas", "8")
                        .with(user("juan@correo.com").roles("CONDUCTOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("kilómetros recorridos")));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void antesDeTerminarMuestraElErrorYNoGuarda() throws Exception {
        reserva.setFechaFin(LocalDate.now().plusDays(3));

        mvc.perform(post("/conductor/servicios/AND-1234/valor-final").with(csrf())
                        .param("kilometros", "100")
                        .param("horas", "8")
                        .with(user("juan@correo.com").roles("CONDUCTOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("cuando termina el servicio")));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void unTuristaNoPuedeVerNiRegistrarValoresFinales() throws Exception {
        mvc.perform(get("/conductor/servicios").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/"));
        mvc.perform(post("/conductor/servicios/AND-1234/valor-final").with(csrf())
                        .param("kilometros", "100")
                        .param("horas", "8")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/"));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void sinIniciarSesionLoMandaAlLogin() throws Exception {
        mvc.perform(get("/conductor/servicios"))
                .andExpect(status().is3xxRedirection());
    }
}
