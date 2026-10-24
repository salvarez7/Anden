package com.example.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "reservas")
public class Reserva {

    
    public static final String CONFIRMADA = "CONFIRMADA";

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
    private String estado;      
    private LocalDateTime fechaCreacion;

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
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
