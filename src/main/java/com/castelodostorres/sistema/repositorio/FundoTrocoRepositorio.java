package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FundoTrocoRepositorio {

    public void salvar(String data, double valor) throws SQLException { // MÉTODO: grava/atualiza o fundo de um dia (upsert)
        String sql = """
            INSERT INTO fundo_troco (data, valor)
            VALUES (?, ?)
            ON CONFLICT(data) DO UPDATE SET valor = excluded.valor
            """;

        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, data);
            comando.setDouble(2, valor);
            comando.executeUpdate();
        }
    }

    public double buscar(String data) throws SQLException { // MÉTODO: busca o fundo de um dia (0 se não houver)
        String sql = "SELECT valor FROM fundo_troco WHERE data = ?";

        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, data);
            try (ResultSet r = comando.executeQuery()) {
                if (r.next()) {
                    return r.getDouble("valor");
                }
            }
        }
        return 0.0; // dia sem fundo registrado
    }
}