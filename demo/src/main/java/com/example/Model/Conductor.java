package com.example.Model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Datos extra que solo tiene un conductor.
// Se une con el usuario por medio del correo.
@Document(collection = "conductores")
public class Conductor {

    @Id
    private String id;

    private String correo;
    private String nombre;
    private String telefono;
    private int edad;
    private String numeroLicencia;
    private LocalDate fechaExpedicionLicencia;
    private String placaVehiculo;
    private String marcaVehiculo;
    private String modeloVehiculo;
    private int asientosVehiculo;
    private LocalDate fechaVencimientoSoat;
    private LocalDate fechaVencimientoTecnomecanica;
    private String estado; // PENDIENTE_VERIFICACION, APROBADO o RECHAZADO

    public Conductor() {
    }

    public Conductor(String correo, String nombre, String telefono, int edad,
                     String numeroLicencia, LocalDate fechaExpedicionLicencia,
                     String placaVehiculo, String marcaVehiculo, String modeloVehiculo,
                     int asientosVehiculo, LocalDate fechaVencimientoSoat,
                     LocalDate fechaVencimientoTecnomecanica) {
        this.correo = correo;
        this.nombre = nombre;
        this.telefono = telefono;
        this.edad = edad;
        this.numeroLicencia = numeroLicencia;
        this.fechaExpedicionLicencia = fechaExpedicionLicencia;
        this.placaVehiculo = placaVehiculo;
        this.marcaVehiculo = marcaVehiculo;
        this.modeloVehiculo = modeloVehiculo;
        this.asientosVehiculo = asientosVehiculo;
        this.fechaVencimientoSoat = fechaVencimientoSoat;
        this.fechaVencimientoTecnomecanica = fechaVencimientoTecnomecanica;
        this.estado = "PENDIENTE_VERIFICACION"; // RF-07
    }

    public String getId() { return id; }
    public String getCorreo() { return correo; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public int getEdad() { return edad; }
    public String getNumeroLicencia() { return numeroLicencia; }
    public LocalDate getFechaExpedicionLicencia() { return fechaExpedicionLicencia; }
    public String getPlacaVehiculo() { return placaVehiculo; }
    public String getMarcaVehiculo() { return marcaVehiculo; }
    public String getModeloVehiculo() { return modeloVehiculo; }
    public int getAsientosVehiculo() { return asientosVehiculo; }
    public LocalDate getFechaVencimientoSoat() { return fechaVencimientoSoat; }
    public LocalDate getFechaVencimientoTecnomecanica() { return fechaVencimientoTecnomecanica; }
    public String getEstado() { return estado; }

    public void setId(String id) { this.id = id; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEdad(int edad) { this.edad = edad; }
    public void setNumeroLicencia(String numeroLicencia) { this.numeroLicencia = numeroLicencia; }
    public void setFechaExpedicionLicencia(LocalDate f) { this.fechaExpedicionLicencia = f; }
    public void setPlacaVehiculo(String placaVehiculo) { this.placaVehiculo = placaVehiculo; }
    public void setMarcaVehiculo(String marcaVehiculo) { this.marcaVehiculo = marcaVehiculo; }
    public void setModeloVehiculo(String modeloVehiculo) { this.modeloVehiculo = modeloVehiculo; }
    public void setAsientosVehiculo(int asientosVehiculo) { this.asientosVehiculo = asientosVehiculo; }
    public void setFechaVencimientoSoat(LocalDate f) { this.fechaVencimientoSoat = f; }
    public void setFechaVencimientoTecnomecanica(LocalDate f) { this.fechaVencimientoTecnomecanica = f; }
    public void setEstado(String estado) { this.estado = estado; }
}
