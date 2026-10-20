package com.example.Service;

// Notificaciones programadas: cuántos días antes del servicio se mandan
public enum TipoNotificacion {

    CANCELACION(14),   // recordatorio de cancelación: 2 semanas antes
    CONFIRMACION(3);   // confirmación del servicio: 3 días antes

    private final int diasAntes;

    TipoNotificacion(int diasAntes) {
        this.diasAntes = diasAntes;
    }

    public int getDiasAntes() {
        return diasAntes;
    }
}