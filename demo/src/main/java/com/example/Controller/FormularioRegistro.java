package com.example.Controller;

import com.example.Service.DatosRegistro;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// Guarda lo que el usuario escribe en registro.html

public class FormularioRegistro {

    private String tipoUsuario = "TURISTA"; // si es Turista o conductor
    private String nombre;
    private String correo;
    private String telefono;
    private String password;

    // Solo necesario para conductor
    private Integer edad;
    private String numeroLicencia;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // el <input type="date"> manda "2024-05-31"
    private LocalDate fechaExpedicionLicencia;
    private String placaVehiculo;
    private String marcaVehiculo;
    private String modeloVehiculo;
    private Integer asientosVehiculo;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaVencimientoSoat;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaVencimientoTecnomecanica;

    public boolean esConductor() {
        return "CONDUCTOR".equalsIgnoreCase(tipoUsuario);
    }

    // Convierte el formulario en el paquete de datos que entiende RegistroService
    public DatosRegistro aDatosRegistro() {
        return new DatosRegistro(
                nombre,
                correo,
                telefono,
                password,
                tipoUsuario,
                edad == null ? 0 : edad,
                numeroLicencia,
                fechaExpedicionLicencia,
                placaVehiculo,
                marcaVehiculo,
                modeloVehiculo,
                asientosVehiculo == null ? 0 : asientosVehiculo,
                fechaVencimientoSoat,
                fechaVencimientoTecnomecanica);
    }

    public String getTipoUsuario() { return tipoUsuario; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getPassword() { return password; }
    public Integer getEdad() { return edad; }
    public String getNumeroLicencia() { return numeroLicencia; }
    public LocalDate getFechaExpedicionLicencia() { return fechaExpedicionLicencia; }
    public String getPlacaVehiculo() { return placaVehiculo; }
    public String getMarcaVehiculo() { return marcaVehiculo; }
    public String getModeloVehiculo() { return modeloVehiculo; }
    public Integer getAsientosVehiculo() { return asientosVehiculo; }
    public LocalDate getFechaVencimientoSoat() { return fechaVencimientoSoat; }
    public LocalDate getFechaVencimientoTecnomecanica() { return fechaVencimientoTecnomecanica; }

    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setPassword(String password) { this.password = password; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public void setNumeroLicencia(String numeroLicencia) { this.numeroLicencia = numeroLicencia; }
    public void setFechaExpedicionLicencia(LocalDate f) { this.fechaExpedicionLicencia = f; }
    public void setPlacaVehiculo(String placaVehiculo) { this.placaVehiculo = placaVehiculo; }
    public void setMarcaVehiculo(String marcaVehiculo) { this.marcaVehiculo = marcaVehiculo; }
    public void setModeloVehiculo(String modeloVehiculo) { this.modeloVehiculo = modeloVehiculo; }
    public void setAsientosVehiculo(Integer asientosVehiculo) { this.asientosVehiculo = asientosVehiculo; }
    public void setFechaVencimientoSoat(LocalDate f) { this.fechaVencimientoSoat = f; }
    public void setFechaVencimientoTecnomecanica(LocalDate f) { this.fechaVencimientoTecnomecanica = f; }
}
