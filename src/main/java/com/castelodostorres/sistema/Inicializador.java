package com.castelodostorres.sistema;

import com.castelodostorres.sistema.banco.CriadorDeSchema;

public class Inicializador {

    public static void main(String[] args) throws Exception{
        CriadorDeSchema.criarTabelas();
        Main.main(args);
    }
}
