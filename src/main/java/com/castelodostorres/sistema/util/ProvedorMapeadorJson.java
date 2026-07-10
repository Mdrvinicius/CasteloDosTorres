package com.castelodostorres.sistema.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ProvedorMapeadorJson {

    private static final ObjectMapper INSTANCIA = new ObjectMapper();

    private ProvedorMapeadorJson(){

    }

    public static ObjectMapper get() {
        return INSTANCIA;
    }



}
