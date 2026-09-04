package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.PagamentoFuncionario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagamentoFuncionarioRepositorio {

    public void salvar(PagamentoFuncionario p) throws SQLException { // MÉTODO: registra um pagamento
        String sql = """
            INSERT INTO pagamento_funcionario (funcionario_id, nome_funcionario, mes_referencia, valor, data_hora_registro)
            VALUES (?, ?, ?, ?, ?)
            """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setInt(1, p.getFuncionarioId());
            cmd.setString(2, p.getNomeFuncionario());
            cmd.setString(3, p.getMesReferencia());
            cmd.setDouble(4, p.getValor());
            cmd.setString(5, p.getDataHoraRegistro());
            cmd.executeUpdate();
            try (ResultSet chaves = cmd.getGeneratedKeys()) {
                if (chaves.next()) p.setId(chaves.getInt(1));
            }
        }
    }

    public void atualizar(PagamentoFuncionario p) throws SQLException { // MÉTODO: edita um pagamento (valor, mês, funcionário)
        String sql = """
            UPDATE pagamento_funcionario
            SET funcionario_id = ?, nome_funcionario = ?, mes_referencia = ?, valor = ?
            WHERE id = ?
            """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setInt(1, p.getFuncionarioId());
            cmd.setString(2, p.getNomeFuncionario());
            cmd.setString(3, p.getMesReferencia());
            cmd.setDouble(4, p.getValor());
            cmd.setInt(5, p.getId());
            cmd.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException { // MÉTODO: remove um pagamento
        String sql = "DELETE FROM pagamento_funcionario WHERE id = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setInt(1, id);
            cmd.executeUpdate();
        }
    }

    public List<PagamentoFuncionario> listarDoMes(String mes) throws SQLException { // MÉTODO: pagamentos com mês de referência = mes
        String sql = "SELECT * FROM pagamento_funcionario WHERE mes_referencia = ? ORDER BY data_hora_registro DESC";
        List<PagamentoFuncionario> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setString(1, mes);
            try (ResultSet r = cmd.executeQuery()) {
                while (r.next()) lista.add(mapear(r));
            }
        }
        return lista;
    }

    public List<PagamentoFuncionario> listarTodos() throws SQLException { // MÉTODO: todos os pagamentos (histórico)
        String sql = "SELECT * FROM pagamento_funcionario ORDER BY data_hora_registro DESC";
        List<PagamentoFuncionario> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql);
             ResultSet r = cmd.executeQuery()) {
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    public Map<Integer, Double> somarPorFuncionarioNoMes(String mes) throws SQLException { // MÉTODO: total pago por funcionário no mês (id -> valor)
        String sql = """
            SELECT funcionario_id, COALESCE(SUM(valor), 0) AS total
            FROM pagamento_funcionario
            WHERE mes_referencia = ?
            GROUP BY funcionario_id
            """;
        Map<Integer, Double> mapa = new HashMap<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setString(1, mes);
            try (ResultSet r = cmd.executeQuery()) {
                while (r.next()) {
                    mapa.put(r.getInt("funcionario_id"), r.getDouble("total"));
                }
            }
        }
        return mapa;
    }

    private PagamentoFuncionario mapear(ResultSet r) throws SQLException {
        PagamentoFuncionario p = new PagamentoFuncionario();
        p.setId(r.getInt("id"));
        p.setFuncionarioId(r.getInt("funcionario_id"));
        p.setNomeFuncionario(r.getString("nome_funcionario"));
        p.setMesReferencia(r.getString("mes_referencia"));
        p.setValor(r.getDouble("valor"));
        p.setDataHoraRegistro(r.getString("data_hora_registro"));
        return p;
    }
}