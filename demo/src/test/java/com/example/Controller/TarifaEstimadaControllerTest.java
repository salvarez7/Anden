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
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.mongodb.uri=mongodb://localhost:27017/pruebas")
class TarifaEstimadaControllerTest {

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
    private Conductor juan;
    private final LocalDate inicio = LocalDate.now().plusDays(10);

    @BeforeEach
    void preparar() {
        duster = new Vehiculo("juan@correo.com", "Renault Duster", "estandar", 5);
        duster.setId("v1");
        juan = new Conductor();
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
    void antesDeConfirmarMuestraElValorEstimadoYElAviso() throws Exception {
        // estándar: 4 días × 6 h × 12.000 = 288.000 + 152 km × 1.450 = 220.400, + recargo de hora pico 33.900
        mvc.perform(post("/reservas/nueva").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(3).toString())
                        .param("pasajeros", "3")
                        .param("trayectos", "Aeropuerto a hotel")
                        .param("numeroTrayectos", "6")
                        .param("trayectosFuera", "1")
                        .param("horasPorDia", "6")
                        .param("horaInicio", "7")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Valor estimado del servicio")))
                .andExpect(content().string(containsString("$542.300")))
                .andExpect(content().string(containsString("Entre $461.000 y $624.000")))
                .andExpect(content().string(containsString("Recargo hora pico y noche")))
                .andExpect(content().string(containsString("solo una estimación")))
                .andExpect(content().string(containsString("Confirmar reserva")));
    }

    @Test
    void sinNumeroDeTrayectosMuestraErrorYNoHayEstimado() throws Exception {
        mvc.perform(post("/reservas/nueva").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(3).toString())
                        .param("pasajeros", "3")
                        .param("trayectos", "Aeropuerto a hotel")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("trayectos vas a hacer dentro de Medellín")))
                .andExpect(content().string(not(containsString("Valor estimado del servicio"))));
    }

    @Test
    void alConfirmarSeGuardaElValorEstimadoEnLaReserva() throws Exception {
        mvc.perform(post("/reservas/confirmar").with(csrf())
                        .param("vehiculoId", "v1")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(3).toString())
                        .param("pasajeros", "3")
                        .param("trayectos", "Aeropuerto a hotel")
                        .param("numeroTrayectos", "6")
                        .param("trayectosFuera", "1")
                        .param("horasPorDia", "6")
                        .param("horaInicio", "7")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Reserva> guardada = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).save(guardada.capture());
        assertEquals(6, guardada.getValue().getNumeroTrayectos());
        assertEquals(1, guardada.getValue().getTrayectosFuera());
        assertEquals(6, guardada.getValue().getHorasPorDia());
        assertEquals(7, guardada.getValue().getHoraInicio());
        assertEquals(542_300L, guardada.getValue().getValorEstimado());
    }

    @Test
    void elTicketMuestraElValorEstimado() throws Exception {
        Reserva reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                inicio, inicio.plusDays(3), 3, "Aeropuerto a hotel");
        reserva.setValorEstimado(542_300L);
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(reserva));

        mvc.perform(get("/reservas/AND-1234").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Valor estimado")))
                .andExpect(content().string(containsString("$542.300")));
    }

    @Test
    void laBusquedaSoloMuestraConductoresAprobadosConSuTarifa() throws Exception {
        Vehiculo spark = new Vehiculo("luis@correo.com", "Chevrolet Spark", "economica", 4);
        spark.setId("v2");
        Conductor luis = new Conductor();
        luis.setCorreo("luis@correo.com");
        luis.setNombre("Luis Pendiente");
        luis.setEstado("PENDIENTE_VERIFICACION");
        when(conductorRepository.findByCorreo("luis@correo.com")).thenReturn(Optional.of(luis));
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(1)).thenReturn(List.of(duster, spark));

        mvc.perform(get("/vehiculos").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Renault Duster")))
                .andExpect(content().string(containsString("$12.000 por hora + recorrido")))
                .andExpect(content().string(not(containsString("Chevrolet Spark"))))
                .andExpect(content().string(not(containsString("Luis Pendiente"))));
    }
}
