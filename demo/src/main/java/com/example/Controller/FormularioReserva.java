package com.example.Controller;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class FormularioReserva {

    private String vehiculoId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    private Integer pasajeros;
    private String trayectos;

    public String getVehiculoId() { return vehiculoId; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public Integer getPasajeros() { return pasajeros; }
    public String getTrayectos() { return trayectos; }

    public void setVehiculoId(String vehiculoId) { this.vehiculoId = vehiculoId; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public void setPasajeros(Integer pasajeros) { this.pasajeros = pasajeros; }
    public void setTrayectos(String trayectos) { this.trayectos = trayectos; }
}