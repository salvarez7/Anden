package com.example.Model;

public enum Gama {
// enum es porque son valores fijos que no cambia, esas siempre seran las 3 gamas que dijimos en el documento
// Sprint 6 (H12): cada gama tiene su tarifa por hora (tiempo del conductor) y por km (gasolina, desgaste y
// recorrido), en pesos. Referencia: InDriver Medellín - Guatapé cuesta entre $120.000 y $150.000
    ECONOMICA("economica", "Económica", 10000, 1250),
    ESTANDAR("estandar", "Estándar", 12000, 1450),
    PREMIUM("premium", "Premium", 16000, 1900);

    private final String codigo; // es para lo que se guarda en la BD
    private final String etiqueta; // y esta para lo que se le muestra al usuario
    private final int tarifaHora;
    private final int tarifaKm;

    Gama(String codigo, String etiqueta, int tarifaHora, int tarifaKm) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
        this.tarifaHora = tarifaHora;
        this.tarifaKm = tarifaKm;
    }
    public String getCodigo() { return codigo; }
    public String getEtiqueta() { return etiqueta; }
    public int getTarifaHora() { return tarifaHora; }
    public int getTarifaKm() { return tarifaKm; }

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
