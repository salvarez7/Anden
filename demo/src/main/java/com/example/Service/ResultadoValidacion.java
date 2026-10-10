package com.example.Service;

import java.util.List;

// valido = true si cumple todo; si no, "errores" trae los mensajes para mostrar
public record ResultadoValidacion(boolean valido, List<String> errores) {
}
