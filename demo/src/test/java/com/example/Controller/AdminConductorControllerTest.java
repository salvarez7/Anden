package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
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
class AdminConductorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ConductorRepository conductorRepository;

    @MockitoBean
    private VehiculoRepository vehiculoRepository;

    private Conductor juan;

    @BeforeEach
    void preparar() {
        juan = new Conductor();
        juan.setId("c1");
        juan.setCorreo("juan@correo.com");
        juan.setNombre("Juan Pérez");
        juan.setNumeroLicencia("LIC-998877");
        juan.setPlacaVehiculo("ABC123");
        juan.setFechaVencimientoSoat(LocalDate.now().plusMonths(6));
        juan.setFechaVencimientoTecnomecanica(LocalDate.now().plusMonths(6));
        juan.setEstado("PENDIENTE_VERIFICACION");

        Vehiculo duster = new Vehiculo("juan@correo.com", "Renault Duster", "estandar", 5);
        duster.getFotos().add("f1");

        when(conductorRepository.findById("c1")).thenReturn(Optional.of(juan));
        when(conductorRepository.findByEstado("PENDIENTE_VERIFICACION")).thenReturn(List.of(juan));
        when(conductorRepository.save(any(Conductor.class))).thenAnswer(i -> i.getArgument(0));
        when(vehiculoRepository.findByCorreoConductor("juan@correo.com")).thenReturn(List.of(duster));
    }

    @Test
    void elAdminVeLaListaDeConductoresPendientes() throws Exception {
        mvc.perform(get("/admin/conductores").with(user("admin@x.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Juan Pérez")));
    }

    @Test
    void elAdminVeLosDocumentosYLasFotosDelConductor() throws Exception {
        mvc.perform(get("/admin/conductores/c1").with(user("admin@x.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("LIC-998877")))
                .andExpect(content().string(containsString("ABC123")))
                .andExpect(content().string(containsString("/fotos/f1")));
    }

    @Test
    void aprobarCambiaElEstadoDelConductor() throws Exception {
        mvc.perform(post("/admin/conductores/c1/decision").with(csrf())
                        .param("decision", "APROBAR")
                        .with(user("admin@x.com").roles("ADMIN")))
                .andExpect(redirectedUrl("/admin/conductores/c1"));

        verify(conductorRepository).save(juan);
        assertThat(juan.getEstado()).isEqualTo("APROBADO");
    }

    @Test
    void rechazarCambiaElEstadoDelConductor() throws Exception {
        mvc.perform(post("/admin/conductores/c1/decision").with(csrf())
                        .param("decision", "RECHAZAR")
                        .with(user("admin@x.com").roles("ADMIN")))
                .andExpect(redirectedUrl("/admin/conductores/c1"));

        assertThat(juan.getEstado()).isEqualTo("RECHAZADO");
    }

    @Test
    void unTuristaNoPuedeEntrarAlPanelDeAdministrador() throws Exception {
        mvc.perform(get("/admin/conductores").with(user("ana@x.com").roles("TURISTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinIniciarSesionLoMandaAlLogin() throws Exception {
        mvc.perform(get("/admin/conductores"))
                .andExpect(status().is3xxRedirection());
    }
}