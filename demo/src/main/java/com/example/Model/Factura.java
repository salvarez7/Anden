package com.example.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// factura simulada
@Document(collection = "facturas")
public class Factura {

    @Id
    private String id;

    private String numero;
    private String codigoReserva;
    private String correoTurista;
    private LocalDateTime fechaEmision;
    private long valorEstimado;
    private int porcentaje;
    private long valorPagado;
    private String metodoPago;
    private String titular;
    private String referenciaPago;
    private LocalDateTime fechaPago;

    public Factura() {
    }

    public Factura(String numero, String codigoReserva, String correoTurista, long valorEstimado, int porcentaje,
                   long valorPagado, String metodoPago, String titular, String referenciaPago, LocalDateTime fechaPago) {
        this.numero = numero;
        this.codigoReserva = codigoReserva;
        this.correoTurista = correoTurista;
        this.valorEstimado = valorEstimado;
        this.porcentaje = porcentaje;
        this.valorPagado = valorPagado;
        this.metodoPago = metodoPago;
        this.titular = titular;
        this.referenciaPago = referenciaPago;
        this.fechaPago = fechaPago;
        this.fechaEmision = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getNumero() { return numero; }
    public String getCodigoReserva() { return codigoReserva; }
    public String getCorreoTurista() { return correoTurista; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public long getValorEstimado() { return valorEstimado; }
    public int getPorcentaje() { return porcentaje; }
    public long getValorPagado() { return valorPagado; }
    public String getMetodoPago() { return metodoPago; }
    public String getTitular() { return titular; }
    public String getReferenciaPago() { return referenciaPago; }
    public LocalDateTime getFechaPago() { return fechaPago; }

    public void setId(String id) { this.id = id; }
}
