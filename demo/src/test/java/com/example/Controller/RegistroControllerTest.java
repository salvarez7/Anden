package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Model.Usuario;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.VehiculoRepository;
import com.example.demo.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;


@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.mongodb.uri=mongodb://localhost:27017/pruebas")
class RegistroControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private ConductorRepository conductorRepository;

    @MockitoBean
    private VehiculoRepository vehiculoRepository;

    @Test
    void elFormularioDeRegistroSeVeSinIniciarSesion() throws Exception {
        mvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Crea tu cuenta de turista")));

        mvc.perform(get("/registro").param("tipo", "CONDUCTOR"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Vencimiento del SOAT")));
    }

    @Test
    void turistaValidoSeGuardaYVaAlLogin() throws Exception {
        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.empty());

        mvc.perform(post("/registro").with(csrf())
                        .param("tipoUsuario", "TURISTA")
                        .param("nombre", "Ana Gómez")
                        .param("correo", "ana@correo.com")
                        .param("telefono", "300 123 4567")
                        .param("password", "secreta1"))
                .andExpect(redirectedUrl("/login?registrado"));

        verify(usuarioRepository).save(any(Usuario.class));
        verify(conductorRepository, never()).save(any());
    }

    @Test
    void correoRepetidoMuestraMensaje() throws Exception {
        when(usuarioRepository.findByCorreo("ana@correo.com"))
                .thenReturn(Optional.of(new Usuario("Ana", "ana@correo.com", "x", "TURISTA")));

        mvc.perform(post("/registro").with(csrf())
                        .param("tipoUsuario", "TURISTA")
                        .param("nombre", "Ana Gómez")
                        .param("correo", "ana@correo.com")
                        .param("telefono", "3001234567")
                        .param("password", "secreta1"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andExpect(content().string(containsString("Ya existe una cuenta con ese correo.")));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void telefonoInvalidoMuestraMensaje() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("tipoUsuario", "TURISTA")
                        .param("nombre", "Ana Gómez")
                        .param("correo", "ana@correo.com")
                        .param("telefono", "12345")
                        .param("password", "secreta1"))
                .andExpect(view().name("registro"))
                .andExpect(content().string(containsString("10 dígitos")));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void conductorConLicenciaDeUnAnoNoSeGuarda() throws Exception {
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());

        mvc.perform(post("/registro").with(csrf())
                        .param("tipoUsuario", "CONDUCTOR")
                        .param("nombre", "Juan Pérez")
                        .param("correo", "juan@correo.com")
                        .param("telefono", "3109876543")
                        .param("password", "secreta1")
                        .param("edad", "30")
                        .param("numeroLicencia", "LIC123")
                        .param("fechaExpedicionLicencia", LocalDate.now().minusYears(1).toString())
                        .param("placaVehiculo", "abc123")
                        .param("asientosVehiculo", "4")
                        .param("gamaVehiculo", "estandar")
                        .param("fechaVencimientoSoat", LocalDate.now().plusMonths(6).toString())
                        .param("fechaVencimientoTecnomecanica", LocalDate.now().plusMonths(6).toString()))
                .andExpect(view().name("registro"))
                .andExpect(content().string(containsString("mínimo 2 años")));

        verify(usuarioRepository, never()).save(any());
        verify(conductorRepository, never()).save(any());
    }

    @Test
    void conductorValidoQuedaPendienteDeVerificacion() throws Exception {
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());

        mvc.perform(post("/registro").with(csrf())
                        .param("tipoUsuario", "CONDUCTOR")
                        .param("nombre", "Juan Pérez")
                        .param("correo", "juan@correo.com")
                        .param("telefono", "3109876543")
                        .param("password", "secreta1")
                        .param("edad", "30")
                        .param("numeroLicencia", "LIC123")
                        .param("fechaExpedicionLicencia", LocalDate.now().minusYears(5).toString())
                        .param("placaVehiculo", "abc123")
                        .param("asientosVehiculo", "4")
                        .param("gamaVehiculo", "estandar")
                        .param("fechaVencimientoSoat", LocalDate.now().plusMonths(6).toString())
                        .param("fechaVencimientoTecnomecanica", LocalDate.now().plusMonths(6).toString()))
                .andExpect(redirectedUrl("/login?registrado"));

        verify(usuarioRepository).save(any(Usuario.class));
        verify(conductorRepository).save(any(Conductor.class));
        verify(vehiculoRepository).save(argThat((Vehiculo v) ->
                v.getGama().equals("estandar") && v.getNumeroAsientos() == 4
                        && v.getCorreoConductor().equals("juan@correo.com")));
    }

    @Test
    void elPerfilDelTuristaMuestraSusDatos() throws Exception {
        when(usuarioRepository.findByCorreo("ana@correo.com"))
                .thenReturn(Optional.of(new Usuario("Ana Gómez", "ana@correo.com", "3001234567", "x", "TURISTA")));

        mvc.perform(get("/turista").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("3001234567")));
    }

    @Test
    void conductorSinGamaNoSeGuarda() throws Exception {
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());

        mvc.perform(post("/registro").with(csrf())
                        .param("tipoUsuario", "CONDUCTOR")
                        .param("nombre", "Juan Pérez")
                        .param("correo", "juan@correo.com")
                        .param("telefono", "3109876543")
                        .param("password", "secreta1")
                        .param("edad", "30")
                        .param("numeroLicencia", "LIC123")
                        .param("fechaExpedicionLicencia", LocalDate.now().minusYears(5).toString())
                        .param("placaVehiculo", "abc123")
                        .param("asientosVehiculo", "4")
                        .param("fechaVencimientoSoat", LocalDate.now().plusMonths(6).toString())
                        .param("fechaVencimientoTecnomecanica", LocalDate.now().plusMonths(6).toString()))
                .andExpect(view().name("registro"))
                .andExpect(content().string(containsString("Elige la gama de tu vehículo.")));

        verify(usuarioRepository, never()).save(any());
        verify(vehiculoRepository, never()).save(any());
    }
}
