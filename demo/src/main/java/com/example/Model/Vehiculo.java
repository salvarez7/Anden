package com.example.Model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vehiculos")
public class Vehiculo {

    @Id
    private String id;

    private String correoConductor; 
    private String modelo;          
    private String gama;            
    private int numeroAsientos;
    private boolean disponible = true; 
    private List<String> fotos = new ArrayList<>();
    private List<PeriodoOcupado> periodosOcupados = new ArrayList<>();

    public Vehiculo() {
    }

    public Vehiculo(String correoConductor, String modelo, String gama, int numeroAsientos) {
        this.correoConductor = correoConductor;
        this.modelo = modelo;
        this.gama = gama;
        this.numeroAsientos = numeroAsientos;
    }

    public String getId() { return id; }
    public String getCorreoConductor() { return correoConductor; }
    public String getModelo() { return modelo; }
    public String getGama() { return gama; }
    public int getNumeroAsientos() { return numeroAsientos; }
    public boolean isDisponible() { return disponible; }
    public List<String> getFotos() { return fotos; }
    public List<PeriodoOcupado> getPeriodosOcupados() { return periodosOcupados; }

    public void setId(String id) { this.id = id; }
    public void setCorreoConductor(String correoConductor) { this.correoConductor = correoConductor; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public void setGama(String gama) { this.gama = gama; }
    public void setNumeroAsientos(int numeroAsientos) { this.numeroAsientos = numeroAsientos; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public void setFotos(List<String> fotos) { this.fotos = fotos; }
    public void setPeriodosOcupados(List<PeriodoOcupado> p) { this.periodosOcupados = p; }
}
