package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.Funcionario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FuncionarioRepositorio {

    public void salvar(Funcionario funcionario) throws SQLException { // MÉTODO: insere um novo funcionário no banco
        String sql = "INSERT INTO funcionario (nome, papel, tipo_remuneracao, valor_remuneracao) VALUES (?, ?, ?, ?)";

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setString(1, funcionario.getNome());
            comando.setString(2, funcionario.getPapel());
            comando.setString(3, funcionario.getTipoRemuneracao());
            comando.setDouble(4, funcionario.getValorRemuneracao());

            comando.executeUpdate();

            try (ResultSet chavesGeradas = comando.getGeneratedKeys()) {
                if (chavesGeradas.next()) {
                    funcionario.setId(chavesGeradas.getInt(1));
                }
            }
        }
    }
}