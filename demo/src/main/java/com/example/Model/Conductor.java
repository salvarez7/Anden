package com.example.Model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Datos extra que solo tiene un conductor.
// Se une con su Usuario (el del login) por medio del correo.
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
    private String estado; // PENDIENTE_VERIFICACION, APROBADO o RECHAZADO

    public Conductor() {
    }

    public Conductor(String correo, String nombre, String telefono, int edad,
                     String numeroLicencia, LocalDate fechaExpedicionLicencia,
                     String placaVehiculo) {
        this.correo = correo;
        this.nombre = nombre;
        this.telefono = telefono;
        this.edad = edad;
        this.numeroLicencia = numeroLicencia;
        this.fechaExpedicionLicencia = fechaExpedicionLicencia;
        this.placaVehiculo = placaVehiculo;
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
    public String getEstado() { return estado; }

    public void setId(String id) { this.id = id; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEdad(int edad) { this.edad = edad; }
    public void setNumeroLicencia(String numeroLicencia) { this.numeroLicencia = numeroLicencia; }
    public void setFechaExpedicionLicencia(LocalDate f) { this.fechaExpedicionLicencia = f; }
    public void setPlacaVehiculo(String placaVehiculo) { this.placaVehiculo = placaVehiculo; }
    public void setEstado(String estado) { this.estado = estado; }
}
