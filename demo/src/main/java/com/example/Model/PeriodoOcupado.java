package com.example.Model;

import java.time.LocalDate;


public class PeriodoOcupado {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String correoTurista;

    public PeriodoOcupado() {
    }

    public PeriodoOcupado(LocalDate fechaInicio, LocalDate fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public PeriodoOcupado(LocalDate fechaInicio, LocalDate fechaFin, String correoTurista) {
        this(fechaInicio, fechaFin);
        this.correoTurista = correoTurista;
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getCorreoTurista() { return correoTurista; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public void setCorreoTurista(String correoTurista) { this.correoTurista = correoTurista; }
}
