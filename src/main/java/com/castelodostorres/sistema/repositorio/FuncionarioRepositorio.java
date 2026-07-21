package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.Funcionario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
    public List<Funcionario> listarPorPapel(String papel) throws SQLException { // MÉTODO: busca funcionários de um papel específico
        String sql = "SELECT id, nome, papel, tipo_remuneracao, valor_remuneracao FROM funcionario WHERE papel = ? AND ativo = 1 ";
        List<Funcionario> lista = new ArrayList<>();

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, papel);

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) { // percorre TODAS as linhas encontradas, uma por vez
                    Funcionario funcionario = new Funcionario();
                    funcionario.setId(resultado.getInt("id"));
                    funcionario.setNome(resultado.getString("nome"));
                    funcionario.setPapel(resultado.getString("papel"));
                    funcionario.setTipoRemuneracao(resultado.getString("tipo_remuneracao"));
                    funcionario.setValorRemuneracao(resultado.getDouble("valor_remuneracao"));
                    lista.add(funcionario);
                }
            }
        }

        return lista;
    }

    public List<Funcionario> listarTodos() throws SQLException { // MÉTODO: traz todos os funcionários cadastrados
        String sql = "SELECT id, nome, papel, tipo_remuneracao, valor_remuneracao FROM funcionario WHERE ativo = 1 ORDER BY nome";

        List<Funcionario> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                Funcionario funcionario = new Funcionario();
                funcionario.setId(resultado.getInt("id"));
                funcionario.setNome(resultado.getString("nome"));
                funcionario.setPapel(resultado.getString("papel"));
                funcionario.setTipoRemuneracao(resultado.getString("tipo_remuneracao"));
                funcionario.setValorRemuneracao(resultado.getDouble("valor_remuneracao"));
                lista.add(funcionario);
            }
        }

        return lista;
    }

    public void atualizar(Funcionario funcionario) throws SQLException { // MÉTODO: edita um funcionário existente
        String sql = """
        UPDATE funcionario
        SET nome = ?,
            papel = ?,
            tipo_remuneracao = ?,
            valor_remuneracao = ?
        WHERE id = ?
        """;

        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, funcionario.getNome());
            comando.setString(2, funcionario.getPapel());
            comando.setString(3, funcionario.getTipoRemuneracao());
            comando.setDouble(4, funcionario.getValorRemuneracao());
            comando.setInt(5, funcionario.getId());
            comando.executeUpdate();
        }
    }

    public void desativar(int id) throws SQLException { // MÉTODO: soft delete - marca inativo
        String sql = "UPDATE funcionario SET ativo = 0 WHERE id = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }



}