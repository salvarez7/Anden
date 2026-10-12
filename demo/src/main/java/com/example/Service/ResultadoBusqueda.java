package com.example.Service;

import com.example.Model.Conductor;
import com.example.Model.Gama;
import com.example.Model.Vehiculo;

public record ResultadoBusqueda(Vehiculo vehiculo, Conductor conductor) {

    public String etiquetaGama() {
        Gama gama = Gama.desde(vehiculo.getGama());
        return gama == null ? vehiculo.getGama() : gama.getEtiqueta();
    }
}
