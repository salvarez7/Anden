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
class MisReservasValorFinalTest {

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
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(2), 3, "Aeropuerto a hotel");
        reserva.setValorEstimado(300_000L);

        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(duster));
        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(juan));
        when(usuarioRepository.findByCorreo("ana@correo.com"))
                .thenReturn(Optional.of(new Usuario("Ana", "ana@correo.com", "3109876543", "x", "TURISTA")));
        when(reservaRepository.findByCorreoTurista("ana@correo.com")).thenReturn(List.of(reserva));
    }

    @Test
    void conValorFinalMuestraElEstimadoElFinalYLaDiferencia() throws Exception {
        reserva.setValorFinal(340_000L);

        mvc.perform(get("/reservas").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("$300.000")))
                .andExpect(content().string(containsString("$340.000")))
                .andExpect(content().string(containsString("Subió $40.000 frente al estimado")));
    }

    @Test
    void siElValorFinalBajoLoDiceAsi() throws Exception {
        reserva.setValorFinal(260_000L);

        mvc.perform(get("/reservas").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(content().string(containsString("Bajó $40.000 frente al estimado")));
    }

    @Test
    void sinValorFinalDiceQueSeDefineAlTerminar() throws Exception {
        mvc.perform(get("/reservas").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Se define al terminar el servicio")))
                .andExpect(content().string(not(containsString("frente al estimado"))));
    }
}
