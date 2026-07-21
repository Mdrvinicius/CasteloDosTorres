package com.castelodostorres.sistema.banco;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class GerenciadorConexao {


    private static Connection conexao;
    private static final String URL_BANCO = montarUrlBanco(); // ATRIBUTO

    private GerenciadorConexao(){

    }

    private static String montarUrlBanco() { // MÉTODO: monta o caminho do banco numa pasta segura do usuário
        String pastaUsuario = System.getProperty("user.home"); // ex: C:\Users\Fulano
        java.io.File pastaApp = new java.io.File(pastaUsuario, "CasteloDosTorres"); // C:\Users\Fulano\CasteloDosTorres

        if (!pastaApp.exists()) {
            pastaApp.mkdirs(); // cria a pasta se não existir
        }

        java.io.File arquivoBanco = new java.io.File(pastaApp, "castelodostorres.db");
        return "jdbc:sqlite:" + arquivoBanco.getAbsolutePath();
    }

    public static Connection getConexao() throws SQLException {
            if(conexao == null || conexao.isClosed()){
                conexao = DriverManager.getConnection(URL_BANCO);
                ativarChavesEstrangeiras(conexao);
            }
            return conexao;
    }

    private static void ativarChavesEstrangeiras(Connection conexao) throws SQLException {
        try (Statement comando = conexao.createStatement()) {
            comando.execute("PRAGMA foreign_keys = ON");
        }
    }

}
