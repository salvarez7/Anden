package com.example.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// Pago simulado de una reserva: no se cobra dinero de verdad
@Document(collection = "pagos")
public class Pago {

    public static final String EXITOSO = "EXITOSO";

    @Id
    private String id;

    private String referencia;      // ej. PAG-3F9A21BC
    private String reservaCodigo;   // relaciona el pago con la reserva (Reserva.codigo)
    private String correoTurista;
    private long monto;
    private String titular;       // nombre del titular de la cuenta (dato simulado)
    private String metodo;          // TARJETA, PSE o NEQUI
    private String estado;
    private LocalDateTime fecha;

    public Pago() {
    }

    public Pago(String referencia, String reservaCodigo, String correoTurista, long monto, String metodo) {
        this.referencia = referencia;
        this.reservaCodigo = reservaCodigo;
        this.correoTurista = correoTurista;
        this.monto = monto;
        this.metodo = metodo;
        this.estado = EXITOSO;
        this.fecha = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getReferencia() { return referencia; }
    public String getReservaCodigo() { return reservaCodigo; }
    public String getCorreoTurista() { return correoTurista; }
    public long getMonto() { return monto; }
    public String getMetodo() { return metodo; }
    public String getTitular() { return titular; }
    public String getEstado() { return estado; }
    public LocalDateTime getFecha() { return fecha; }

    public void setId(String id) { this.id = id; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public void setReservaCodigo(String reservaCodigo) { this.reservaCodigo = reservaCodigo; }
    public void setCorreoTurista(String correoTurista) { this.correoTurista = correoTurista; }
    public void setMonto(long monto) { this.monto = monto; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public void setTitular(String titular) { this.titular = titular; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}