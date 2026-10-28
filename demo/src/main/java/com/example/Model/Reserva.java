package com.example.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "reservas")
public class Reserva {

    
    public static final String CONFIRMADA = "CONFIRMADA";
    public static final String PAGO_PENDIENTE = "PENDIENTE";
    public static final String PAGADA = "PAGADA";

    @Id
    private String id;

    private String codigo;      
    private String vehiculoId;
    private String correoConductor;
    private String correoTurista;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int pasajeros;
    private String trayectos;
    // Sprint 6 (Sofía, H12): trayectos (en Medellín y fuera), horas al día, hora de inicio y el valor estimado que vio el turista
    private Integer numeroTrayectos;
    private Integer horasPorDia;
    private Integer trayectosFuera;
    private Integer horaInicio;
    private Long valorEstimado;

    // Sprint 6 (Nikol, H13): lo que realmente se trabajó y el valor final que se define al terminar el servicio
    private Long valorFinal;
    private Integer kilometrosReales;
    private Integer horasReales;
    private LocalDateTime fechaValorFinal;
    private String estado;      
    private LocalDateTime fechaCreacion;

    private String estadoPago;
    private String referenciaPago;

    public Reserva() {
    }

    public Reserva(String codigo, String vehiculoId, String correoConductor, String correoTurista,
                   LocalDate fechaInicio, LocalDate fechaFin, int pasajeros, String trayectos) {
        this.codigo = codigo;
        this.vehiculoId = vehiculoId;
        this.correoConductor = correoConductor;
        this.correoTurista = correoTurista;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.pasajeros = pasajeros;
        this.trayectos = trayectos;
        this.estado = CONFIRMADA;
        this.fechaCreacion = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getVehiculoId() { return vehiculoId; }
    public String getCorreoConductor() { return correoConductor; }
    public String getCorreoTurista() { return correoTurista; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public int getPasajeros() { return pasajeros; }
    public String getTrayectos() { return trayectos; }
    public Integer getNumeroTrayectos() { return numeroTrayectos; }
    public Integer getHorasPorDia() { return horasPorDia; }
    public Integer getTrayectosFuera() { return trayectosFuera; }
    public Integer getHoraInicio() { return horaInicio; }
    public Long getValorEstimado() { return valorEstimado; }
    public Long getValorFinal() { return valorFinal; }
    public Integer getKilometrosReales() { return kilometrosReales; }
    public Integer getHorasReales() { return horasReales; }
    public LocalDateTime getFechaValorFinal() { return fechaValorFinal; }

   
    public Long getDiferenciaConEstimado() {
        if (valorFinal == null || valorEstimado == null) {
            return null;
        }
        return valorFinal - valorEstimado;
    }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public void setId(String id) { this.id = id; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setVehiculoId(String vehiculoId) { this.vehiculoId = vehiculoId; }
    public void setCorreoConductor(String correoConductor) { this.correoConductor = correoConductor; }
    public void setCorreoTurista(String correoTurista) { this.correoTurista = correoTurista; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public void setPasajeros(int pasajeros) { this.pasajeros = pasajeros; }
    public void setTrayectos(String trayectos) { this.trayectos = trayectos; }
    public void setNumeroTrayectos(Integer numeroTrayectos) { this.numeroTrayectos = numeroTrayectos; }
    public void setHorasPorDia(Integer horasPorDia) { this.horasPorDia = horasPorDia; }
    public void setTrayectosFuera(Integer trayectosFuera) { this.trayectosFuera = trayectosFuera; }
    public void setHoraInicio(Integer horaInicio) { this.horaInicio = horaInicio; }
    public void setValorEstimado(Long valorEstimado) { this.valorEstimado = valorEstimado; }
    public void setValorFinal(Long valorFinal) { this.valorFinal = valorFinal; }
    public void setKilometrosReales(Integer kilometrosReales) { this.kilometrosReales = kilometrosReales; }
    public void setHorasReales(Integer horasReales) { this.horasReales = horasReales; }
    public void setFechaValorFinal(LocalDateTime fechaValorFinal) { this.fechaValorFinal = fechaValorFinal; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    // Las reservas anteriores al pago no tienen este campo: se leen como pendientes
    public String getEstadoPago() { return estadoPago == null ? PAGO_PENDIENTE : estadoPago; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
}
