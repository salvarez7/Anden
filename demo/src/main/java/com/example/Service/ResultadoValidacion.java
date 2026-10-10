package com.example.Service;

import java.util.List;


public record ResultadoValidacion(boolean valido, List<String> errores) {
}
