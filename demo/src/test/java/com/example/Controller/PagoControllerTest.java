package com.example.Controller;

import com.example.Model.Reserva;
import com.example.Repository.PagoRepository;
import com.example.Repository.ReservaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.mongodb.uri=mongodb://localhost:27017/pruebas")
class PagoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ReservaRepository reservaRepository;

    @MockitoBean
    private PagoRepository pagoRepository;

    @MockitoBean
    private VehiculoRepository vehiculoRepository;

    private Reserva reserva;

    @BeforeEach
    void preparar() {
        LocalDate inicio = LocalDate.now().plusDays(10);
        reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                inicio, inicio.plusDays(3), 3, "Aeropuerto a hotel");
        reserva.setValorEstimado(300000L);
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(reserva));
    }

    @Test
    void elFormularioMuestraElAnticipoYLosCuatroMetodos() throws Exception {
        mvc.perform(get("/reservas/AND-1234/pago").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AND-1234")))
                .andExpect(content().string(containsString("$300.000")))
                .andExpect(content().string(containsString("$30.000")))
                .andExpect(content().string(containsString("BBVA")))
                .andExpect(content().string(containsString("Bancolombia")))
                .andExpect(content().string(containsString("Banco de Bogotá")))
                .andExpect(content().string(containsString("PayPal")))
                .andExpect(content().string(containsString("Nombre del titular")))
                .andExpect(content().string(containsString("Pagar ahora")))
                .andExpect(content().string(not(containsString("Nequi"))));
    }

    @Test
    void pagarMarcaLaReservaPagadaYVuelveAlTicketConElMensaje() throws Exception {
        mvc.perform(post("/reservas/AND-1234/pago").with(csrf())
                        .param("metodo", "BANCOLOMBIA")
                        .param("titular", "Ana Gómez")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/reservas/AND-1234"))
                .andExpect(flash().attribute("mensajePago", containsString("$30.000")))
                .andExpect(flash().attribute("mensajePago", containsString("Bancolombia")));
        assertThat(reserva.getEstadoPago()).isEqualTo("PAGADA");
        assertThat(reserva.getReferenciaPago()).startsWith("PAG-");
    }

    @Test
    void sinTitularVuelveAlFormularioConElError() throws Exception {
        mvc.perform(post("/reservas/AND-1234/pago").with(csrf())
                        .param("metodo", "BBVA")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/reservas/AND-1234/pago"))
                .andExpect(flash().attributeExists("errorPago"));
        assertThat(reserva.getEstadoPago()).isEqualTo("PENDIENTE");
    }

    @Test
    void unMetodoQueNoEstaEnLaListaSeRechaza() throws Exception {
        mvc.perform(post("/reservas/AND-1234/pago").with(csrf())
                        .param("metodo", "NEQUI")
                        .param("titular", "Ana Gómez")
                        .with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/reservas/AND-1234/pago"))
                .andExpect(flash().attributeExists("errorPago"));
        assertThat(reserva.getEstadoPago()).isEqualTo("PENDIENTE");
    }

    @Test
    void unaReservaAjenaNoSePuedePagar() throws Exception {
        mvc.perform(get("/reservas/AND-1234/pago").with(user("otro@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/vehiculos"));
    }

    @Test
    void sinIniciarSesionLoMandaAlLogin() throws Exception {
        mvc.perform(get("/reservas/AND-1234/pago"))
                .andExpect(status().is3xxRedirection());
    }
}
