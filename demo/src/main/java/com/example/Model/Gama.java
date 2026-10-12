package com.example.Model;

public enum Gama {
// enum es porque son valores fijos que no cambia, esas siempre seran las 3 gamas que dijimos en el documento
    ECONOMICA("economica", "Económica"),
    ESTANDAR("estandar", "Estándar"),
    PREMIUM("premium", "Premium");

    private final String codigo; // es para lo que se guarda en la BD
    private final String etiqueta; // y esta para lo que se le muestra al usuario

    Gama(String codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }
    public String getCodigo() { return codigo; }
    public String getEtiqueta() { return etiqueta; }

    public static Gama desde(String texto) {
        if (texto == null) {
            return null;
        }
        for (Gama gama : values()) {
            if (gama.codigo.equalsIgnoreCase(texto.trim())) {
                return gama;
            }
        }
        return null;
    }
}
