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
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.mongodb.uri=mongodb://localhost:27017/pruebas")
class ConfirmarReservaControllerTest {

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
    void confirmarUnaReservaValidaLlevaAlTicketYMarcaElVehiculoOcupado() throws Exception {
        LocalDate inicio = LocalDate.now().plusDays(10);
        mvc.perform(post("/reservas/confirmar").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(3).toString())
                        .param("pasajeros", "3")
                        .param("trayectos", "Aeropuerto a hotel")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/reservas/AND-*"));

        verify(reservaRepository).save(any(Reserva.class));
        assertEquals(1, duster.getPeriodosOcupados().size());
    }

    @Test
    void confirmarConMasPasajerosQueLosPuestosMuestraErrorYNoGuarda() throws Exception {
        LocalDate inicio = LocalDate.now().plusDays(10);
        mvc.perform(post("/reservas/confirmar").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(3).toString())
                        .param("pasajeros", "9")
                        .param("trayectos", "Aeropuerto a hotel")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("capacidad para 5 pasajeros")));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void elTicketMuestraElCodigoYElEnlaceDeWhatsApp() throws Exception {
        LocalDate inicio = LocalDate.now().plusDays(10);
        Reserva reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                inicio, inicio.plusDays(3), 3, "Aeropuerto a hotel");
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(reserva));

        mvc.perform(get("/reservas/AND-1234").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AND-1234")))
                .andExpect(content().string(containsString("Renault Duster")))
                .andExpect(content().string(containsString("https://wa.me/573001234567")));
    }

    @Test
    void elTicketDeOtroTuristaNoSePuedeVer() throws Exception {
        Reserva reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(13), 3, "Aeropuerto a hotel");
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(reserva));

        mvc.perform(get("/reservas/AND-1234").with(user("luis@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/vehiculos"));
    }

    @Test
    void sinIniciarSesionNoSePuedeConfirmarNiVerElTicket() throws Exception {
        mvc.perform(get("/reservas/AND-1234"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/reservas/confirmar").with(csrf()).param("vehiculoId", "v1"))
                .andExpect(status().is3xxRedirection());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }
}
