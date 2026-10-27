package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.Gama;
import com.example.Model.Vehiculo;

public record ResultadoBusqueda(Vehiculo vehiculo, Conductor conductor) {

    public String etiquetaGama() {
        Gama gama = Gama.desde(vehiculo.getGama());
        return gama == null ? vehiculo.getGama() : gama.getEtiqueta();
    }

    // Sprint 6: tarifa de referencia por hora ("$12.000"), o null si la gama no es conocida
    public String tarifaHora() {
        Gama gama = Gama.desde(vehiculo.getGama());
        return gama == null ? null : TarifaEstimada.pesos(gama.getTarifaHora());
    }
}
