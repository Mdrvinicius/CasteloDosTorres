package com.castelodostorres.sistema.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormatadorData {

    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"); // ATRIBUTO

    public static String formatar(String dataIso) { // MÉTODO: converte ISO -> dd/MM/yyyy HH:mm
        if (dataIso == null || dataIso.isBlank()) {
            return "-";
        }
        try {
            LocalDateTime dataHora = LocalDateTime.parse(dataIso);
            return dataHora.format(FORMATO_BR);
        } catch (Exception e) {
            return dataIso;
        }
    }
}