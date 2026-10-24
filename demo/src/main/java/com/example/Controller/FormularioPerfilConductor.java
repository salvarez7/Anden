package com.example.Controller;

import com.example.Model.Conductor;
import com.example.Service.PerfilConductorService;
import com.example.Service.PreferenciasConductor;

import java.util.ArrayList;
import java.util.List;

// Para guardar los cmabios que hace el conductor
public class FormularioPerfilConductor {

    private Integer añosExperiencia;
    private String zonas;
    private String disponibilidadHoraria;
    private List<String> fotosAQuitar = new ArrayList<>();

    public static FormularioPerfilConductor desde(Conductor conductor) {
        FormularioPerfilConductor f = new FormularioPerfilConductor();
        f.añosExperiencia = conductor.getAñosExperiencia();
        f.zonas = conductor.getZonas() == null ? "" : String.join(", ", conductor.getZonas());
        f.disponibilidadHoraria = conductor.getDisponibilidadHoraria();
        return f;
    }

    public PreferenciasConductor aPreferencias() {
        return new PreferenciasConductor(añosExperiencia, PerfilConductorService.separarZonas(zonas),
                disponibilidadHoraria);
    }

    public Integer getAñosExperiencia() { return añosExperiencia; }
    public String getZonas() { return zonas; }
    public String getDisponibilidadHoraria() { return disponibilidadHoraria; }
    public List<String> getFotosAQuitar() { return fotosAQuitar; }

    public void setAñosExperiencia(Integer añosExperiencia) { this.añosExperiencia = añosExperiencia; }
    public void setZonas(String zonas) { this.zonas = zonas; }
    public void setDisponibilidadHoraria(String disponibilidadHoraria) { this.disponibilidadHoraria = disponibilidadHoraria; }
    public void setFotosAQuitar(List<String> fotosAQuitar) { this.fotosAQuitar = fotosAQuitar; }
}
