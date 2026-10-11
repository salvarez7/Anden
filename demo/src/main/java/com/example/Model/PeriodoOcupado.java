package com.example.Model;

import java.time.LocalDate;


public class PeriodoOcupado {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public PeriodoOcupado() {
    }

    public PeriodoOcupado(LocalDate fechaInicio, LocalDate fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
}
