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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.mongodb.uri=mongodb://localhost:27017/pruebas")
class MisReservasControllerTest {

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

    private Conductor juan;
    private Vehiculo duster;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        duster = new Vehiculo("juan@correo.com", "Renault Duster", "estandar", 5);
        duster.setId("v1");
        juan = new Conductor();
        juan.setCorreo("juan@correo.com");
        juan.setNombre("Juan Pérez");
        juan.setTelefono("3001234567");
        juan.setEstado("APROBADO");
        LocalDate inicio = LocalDate.now().plusDays(10);
        reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                inicio, inicio.plusDays(3), 3, "Aeropuerto a hotel");

        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(duster));
        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(juan));
        when(usuarioRepository.findByCorreo("ana@correo.com"))
                .thenReturn(Optional.of(new Usuario("Ana", "ana@correo.com", "3109876543", "x", "TURISTA")));
    }

    @Test
    void elTuristaVeSusReservasConElBotonDeWhatsApp() throws Exception {
        when(reservaRepository.findByCorreoTurista("ana@correo.com")).thenReturn(List.of(reserva));

        mvc.perform(get("/reservas").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AND-1234")))
                .andExpect(content().string(containsString("Juan Pérez")))
                .andExpect(content().string(containsString("Contactar por WhatsApp")))
                .andExpect(content().string(containsString("https://wa.me/573001234567?text=Hola%20Juan")));
    }

    @Test
    void sinReservasLeDiceQueBusqueUnVehiculo() throws Exception {
        when(reservaRepository.findByCorreoTurista("ana@correo.com")).thenReturn(List.of());

        mvc.perform(get("/reservas").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no tienes reservas")))
                .andExpect(content().string(not(containsString("wa.me"))));
    }

    @Test
    void sinIniciarSesionNoSeVenLasReservas() throws Exception {
        mvc.perform(get("/reservas"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void elTicketTieneElBotonConElMensajeDeLaReserva() throws Exception {
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(reserva));

        mvc.perform(get("/reservas/AND-1234").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contactar por WhatsApp")))
                .andExpect(content().string(containsString("reserva%20AND-1234")));
    }
}
