package com.example.Controller;

import com.example.Model.Conductor;
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

import com.example.Model.PeriodoOcupado;

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
class BusquedaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VehiculoRepository vehiculoRepository;

    @MockitoBean
    private ConductorRepository conductorRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void muestraLosVehiculosQueCumplenLosFiltros() throws Exception {
        Conductor juan = new Conductor();
        juan.setCorreo("juan@correo.com");
        juan.setNombre("Juan Pérez");
        juan.setEstado("APROBADO");
        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(juan));
        when(vehiculoRepository.findByGamaAndNumeroAsientosGreaterThanEqual("premium", 5))
                .thenReturn(List.of(new Vehiculo("juan@correo.com", "Toyota Fortuner", "premium", 7)));

        mvc.perform(get("/vehiculos").param("gama", "premium").param("asientos", "5")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Toyota Fortuner")))
                .andExpect(content().string(containsString("Juan Pérez")))
                .andExpect(content().string(not(containsString("No encontramos vehículos"))));
    }

    @Test
    void sinResultadosMuestraElMensaje() throws Exception {
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(9)).thenReturn(List.of());

        mvc.perform(get("/vehiculos").param("asientos", "9")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No encontramos vehículos disponibles")));
    }

    @Test
    void sinIniciarSesionLoMandaAlLogin() throws Exception {
        mvc.perform(get("/vehiculos"))
                .andExpect(status().is3xxRedirection());
    }

    // Sprint 3: con fechas solo salen los carros libres en esas fechas
    @Test
    void conFechasSoloMuestraLosConductoresLibres() throws Exception {
        LocalDate desde = LocalDate.now().plusDays(10);
        LocalDate hasta = desde.plusDays(2);
        for (String correo : List.of("libre@correo.com", "ocupado@correo.com")) {
            Conductor c = new Conductor();
            c.setCorreo(correo);
            c.setNombre(correo.startsWith("libre") ? "Laura Libre" : "Oscar Ocupado");
            c.setEstado("APROBADO");
            when(conductorRepository.findByCorreo(correo)).thenReturn(Optional.of(c));
        }
        Vehiculo libre = new Vehiculo("libre@correo.com", "Kia Picanto", "economica", 4);
        Vehiculo ocupado = new Vehiculo("ocupado@correo.com", "Mazda CX-5", "estandar", 5);
        ocupado.getPeriodosOcupados().add(new PeriodoOcupado(desde.plusDays(1), hasta.plusDays(3)));
        when(vehiculoRepository.findByNumeroAsientosGreaterThanEqual(1)).thenReturn(List.of(libre, ocupado));

        mvc.perform(get("/vehiculos").param("desde", desde.toString()).param("hasta", hasta.toString())
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Laura Libre")))
                .andExpect(content().string(not(containsString("Oscar Ocupado"))))
                .andExpect(content().string(containsString("Conductores disponibles del " + desde)));
    }

    @Test
    void fechasAlRevesMuestranErrorYNoBusca() throws Exception {
        LocalDate desde = LocalDate.now().plusDays(10);

        mvc.perform(get("/vehiculos").param("desde", desde.toString())
                        .param("hasta", desde.minusDays(3).toString())
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La fecha final debe ser igual o posterior")))
                .andExpect(content().string(not(containsString("No encontramos vehículos"))));
    }
}
