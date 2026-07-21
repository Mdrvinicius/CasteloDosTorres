package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SaidaCaixaRepositorio {

    public void salvar(String data, double valor, String motivo) throws SQLException { // registra uma saída de caixa
        String sql = "INSERT INTO saida_caixa (data, valor, motivo) VALUES (?, ?, ?)";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, data);
            comando.setDouble(2, valor);
            comando.setString(3, motivo);
            comando.executeUpdate();
        }
    }

    public double totalDoDia(String data) throws SQLException { // soma as saídas de um dia
        String sql = "SELECT COALESCE(SUM(valor), 0) AS total FROM saida_caixa WHERE data = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, data);
            try (ResultSet r = comando.executeQuery()) {
                if (r.next()) return r.getDouble("total");
            }
        }
        return 0.0;
    }
}