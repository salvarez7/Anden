package com.example.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Una foto de vehículo. Se guarda aparte para no hacer pesados los documentos de "vehiculos".
@Document(collection = "fotos")
public class FotoVehiculo {

    @Id
    private String id;

    private String tipoContenido; // image/jpeg, image/png o image/webp
    private byte[] datos;

    public FotoVehiculo() {
    }

    public FotoVehiculo(String tipoContenido, byte[] datos) {
        this.tipoContenido = tipoContenido;
        this.datos = datos;
    }

    public String getId() { return id; }
    public String getTipoContenido() { return tipoContenido; }
    public byte[] getDatos() { return datos; }

    public void setId(String id) { this.id = id; }
    public void setTipoContenido(String tipoContenido) { this.tipoContenido = tipoContenido; }
    public void setDatos(byte[] datos) { this.datos = datos; }
}