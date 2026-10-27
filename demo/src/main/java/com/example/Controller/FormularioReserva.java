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
    private Integer numeroTrayectos; // Sprint 6: cuántos trayectos dentro de Medellín, para la tarifa
    private Integer trayectosFuera;  // Sprint 6: cuántos viajes a otro municipio (Guatapé, etc.)
    private Integer horasPorDia;     // Sprint 6: cuántas horas al día necesita el carro
    private Integer horaInicio;      // Sprint 6: a qué hora empieza (0 a 23), para el recargo de pico y noche

    public String getVehiculoId() { return vehiculoId; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public Integer getPasajeros() { return pasajeros; }
    public String getTrayectos() { return trayectos; }
    public Integer getNumeroTrayectos() { return numeroTrayectos; }
    public Integer getHorasPorDia() { return horasPorDia; }
    public Integer getTrayectosFuera() { return trayectosFuera; }
    public Integer getHoraInicio() { return horaInicio; }

    public void setVehiculoId(String vehiculoId) { this.vehiculoId = vehiculoId; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public void setPasajeros(Integer pasajeros) { this.pasajeros = pasajeros; }
    public void setTrayectos(String trayectos) { this.trayectos = trayectos; }
    public void setNumeroTrayectos(Integer numeroTrayectos) { this.numeroTrayectos = numeroTrayectos; }
    public void setHorasPorDia(Integer horasPorDia) { this.horasPorDia = horasPorDia; }
    public void setTrayectosFuera(Integer trayectosFuera) { this.trayectosFuera = trayectosFuera; }
    public void setHoraInicio(Integer horaInicio) { this.horaInicio = horaInicio; }
}