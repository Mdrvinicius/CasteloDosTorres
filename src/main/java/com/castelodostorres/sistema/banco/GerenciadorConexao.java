package com.castelodostorres.sistema.banco;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class GerenciadorConexao {

    private static final String URL_BANCO = "jdbc:sqlite:castelodostorres.db";
    private static Connection conexao;

    private GerenciadorConexao(){

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
