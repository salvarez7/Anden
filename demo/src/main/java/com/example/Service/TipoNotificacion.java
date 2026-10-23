package com.example.Service;

public enum TipoNotificacion {

    CANCELACION(14),
    CONFIRMACION(3);

    private final int diasAntes;

    TipoNotificacion(int diasAntes) {
        this.diasAntes = diasAntes;
    }

    public int getDiasAntes() {
        return diasAntes;
    }
}