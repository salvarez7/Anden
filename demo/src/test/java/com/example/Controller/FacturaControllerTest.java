package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Model.Factura;
import com.example.Model.Pago;
import com.example.Model.Reserva;
import com.example.Model.Usuario;
import com.example.Model.Vehiculo;
import com.example.Repository.ConductorRepository;
import com.example.Repository.FacturaRepository;
import com.example.Repository.PagoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.mongodb.uri=mongodb://localhost:27017/pruebas")
class FacturaControllerTest {

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

    @MockitoBean
    private PagoRepository pagoRepository;

    @MockitoBean
    private FacturaRepository facturaRepository;

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
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(duster));
        when(conductorRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(juan));
        when(usuarioRepository.findByCorreo("ana@correo.com"))
                .thenReturn(Optional.of(new Usuario("Ana", "ana@correo.com", "3109876543", "x", "TURISTA")));

        LocalDate inicio = LocalDate.now().plusDays(10);
        reserva = new Reserva("AND-1234", "v1", "juan@correo.com", "ana@correo.com",
                inicio, inicio.plusDays(3), 3, "Aeropuerto a hotel");
        reserva.setValorEstimado(542_300L);
        reserva.setEstadoPago(Reserva.PAGADA);
        reserva.setReferenciaPago("PAG-1A2B3C4D");
        when(reservaRepository.findByCodigo("AND-1234")).thenReturn(Optional.of(reserva));

        Pago pago = new Pago("PAG-1A2B3C4D", "AND-1234", "ana@correo.com", 54_200, "BANCOLOMBIA");
        pago.setTitular("Ana Gómez");
        when(pagoRepository.findByReservaCodigo("AND-1234")).thenReturn(Optional.of(pago));
        when(facturaRepository.findByCodigoReserva("AND-1234")).thenReturn(Optional.empty());
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void conLaReservaPagadaElTicketMuestraVerFactura() throws Exception {
        mvc.perform(get("/reservas/AND-1234").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ver factura")))
                .andExpect(content().string(containsString("/reservas/AND-1234/factura")));
    }

    @Test
    void sinPagarElTicketNoMuestraVerFactura() throws Exception {
        reserva.setEstadoPago(null);

        mvc.perform(get("/reservas/AND-1234").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pagar ahora")))
                .andExpect(content().string(not(containsString("Ver factura"))));
    }

    @Test
    void misReservasMuestraVerFacturaEnLasPagadas() throws Exception {
        when(reservaRepository.findByCorreoTurista("ana@correo.com")).thenReturn(List.of(reserva));

        mvc.perform(get("/reservas").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/reservas/AND-1234/factura")));
    }

    @Test
    void laFacturaMuestraLosDatosDeLaReservaYDelPago() throws Exception {
        mvc.perform(get("/reservas/AND-1234/factura").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FAC-AND-1234")))
                .andExpect(content().string(containsString("Ana")))
                .andExpect(content().string(containsString("Renault Duster")))
                .andExpect(content().string(containsString("Juan Pérez")))
                .andExpect(content().string(containsString("(4 días)")))
                .andExpect(content().string(containsString("PAG-1A2B3C4D")))
                .andExpect(content().string(containsString("Bancolombia")))
                .andExpect(content().string(containsString("Ana Gómez")))
                .andExpect(content().string(containsString("$542.300")))
                .andExpect(content().string(containsString("$488.100")))
                .andExpect(content().string(containsString("$54.200")))
                .andExpect(content().string(containsString("Factura simulada")))
                .andExpect(content().string(containsString("Imprimir factura")));
        verify(facturaRepository).save(any(Factura.class));
    }

    @Test
    void conValorFinalMuestraElValorFinalYElSaldoReal() throws Exception {
        reserva.setValorFinal(600_000L);

        mvc.perform(get("/reservas/AND-1234/factura").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Valor final del servicio")))
                .andExpect(content().string(containsString("$600.000")))
                .andExpect(content().string(containsString("$545.800")));
    }

    @Test
    void sinPagarLaFacturaVuelveAlTicket() throws Exception {
        reserva.setEstadoPago(null);

        mvc.perform(get("/reservas/AND-1234/factura").with(user("ana@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/reservas/AND-1234"));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    void otroTuristaNoPuedeVerLaFactura() throws Exception {
        mvc.perform(get("/reservas/AND-1234/factura").with(user("otro@correo.com").roles("TURISTA")))
                .andExpect(redirectedUrl("/vehiculos"));
        verify(facturaRepository, never()).save(any());
    }
}
