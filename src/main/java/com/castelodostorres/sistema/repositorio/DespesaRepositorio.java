package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.Despesa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DespesaRepositorio {

    public void salvar(Despesa despesa) throws SQLException { // MÉTODO: insere nova despesa
        String sql = "INSERT INTO despesa (nome, valor, tipo, data_hora_cadastro) VALUES (?, ?, ?, ?)";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, despesa.getNome());
            comando.setDouble(2, despesa.getValor());
            comando.setString(3, despesa.getTipo());
            comando.setString(4, despesa.getDataHoraCadastro());
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) despesa.setId(chaves.getInt(1));
            }
        }
    }

    public void atualizar(Despesa despesa) throws SQLException { // MÉTODO: edita despesa existente
        String sql = "UPDATE despesa SET nome = ?, valor = ?, tipo = ? WHERE id = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, despesa.getNome());
            comando.setDouble(2, despesa.getValor());
            comando.setString(3, despesa.getTipo());
            comando.setInt(4, despesa.getId());
            comando.executeUpdate();
        }
    }

    public void apagar(int id) throws SQLException { // MÉTODO: apaga despesa de vez (não precisa soft delete aqui)
        String sql = "DELETE FROM despesa WHERE id = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }

    public List<Despesa> listarTodas() throws SQLException { // MÉTODO: lista recorrentes primeiro, depois avulsas
        String sql = """
            SELECT id, nome, valor, tipo, data_hora_cadastro
            FROM despesa
            ORDER BY CASE tipo WHEN 'RECORRENTE' THEN 0 ELSE 1 END, data_hora_cadastro DESC
            """;
        List<Despesa> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet r = comando.executeQuery()) {
            while (r.next()) {
                Despesa d = new Despesa();
                d.setId(r.getInt("id"));
                d.setNome(r.getString("nome"));
                d.setValor(r.getDouble("valor"));
                d.setTipo(r.getString("tipo"));
                d.setDataHoraCadastro(r.getString("data_hora_cadastro"));
                lista.add(d);
            }
        }
        return lista;
    }

    public List<Despesa> listarDoMes(String mes) throws SQLException { // mes = "aaaa-mm"
        String sql = """
            SELECT d.id, d.nome, d.tipo, d.data_hora_cadastro,
                   COALESCE(
                       (SELECT dm.valor
                        FROM despesa_mensal dm
                        WHERE dm.despesa_id = d.id AND dm.mes <= ?
                        ORDER BY dm.mes DESC
                        LIMIT 1),
                       d.valor
                   ) AS valor_efetivo
            FROM despesa d
            WHERE
                (d.tipo = 'AVULSA'     AND strftime('%Y-%m', d.data_hora_cadastro) = ?)
             OR (d.tipo = 'RECORRENTE' AND strftime('%Y-%m', d.data_hora_cadastro) <= ?)
            ORDER BY CASE d.tipo WHEN 'RECORRENTE' THEN 0 ELSE 1 END, d.data_hora_cadastro DESC
            """;

        List<Despesa> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, mes); // ajuste mensal
            comando.setString(2, mes); // avulsa: cadastrada no mês
            comando.setString(3, mes); // recorrente: cadastrada até o mês
            try (ResultSet r = comando.executeQuery()) {
                while (r.next()) {
                    Despesa d = new Despesa();
                    d.setId(r.getInt("id"));
                    d.setNome(r.getString("nome"));
                    d.setTipo(r.getString("tipo"));
                    d.setDataHoraCadastro(r.getString("data_hora_cadastro"));
                    d.setValor(r.getDouble("valor_efetivo")); // já é o valor ajustado ou o padrão
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    public void salvarAjusteMensal(int despesaId, String mes, double valor) throws SQLException { // upsert do ajuste por despesa+mês
        String sql = """
        INSERT INTO despesa_mensal (despesa_id, mes, valor)
        VALUES (?, ?, ?)
        ON CONFLICT(despesa_id, mes) DO UPDATE SET valor = excluded.valor
        """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, despesaId);
            comando.setString(2, mes);
            comando.setDouble(3, valor);
            comando.executeUpdate();
        }
    }

    public void apagarAjustesDaDespesa(int despesaId) throws SQLException { // remove os ajustes mensais de uma despesa
        String sql = "DELETE FROM despesa_mensal WHERE despesa_id = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, despesaId);
            comando.executeUpdate();
        }
    }
}