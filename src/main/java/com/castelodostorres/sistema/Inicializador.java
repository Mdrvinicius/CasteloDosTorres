package com.castelodostorres.sistema;

import com.castelodostorres.sistema.banco.MigradorDeSchema;

public class Inicializador {
    public static void main(String[] args) throws Exception {
        MigradorDeSchema.migrar(); // no lugar de CriadorDeSchema.criarTabelas()
        Main.main(args);
    }
}