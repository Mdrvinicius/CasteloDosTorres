package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.FechamentoCaixa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FechamentoCaixaRepositorio {

    public void salvar(String data, String dataHoraFechamento, Integer funcionarioId, String nomeFuncionario,
                       double dinheiroEsperado, double dinheiroContado,
                       double pixdebitoEsperado, double pixdebitoContado) throws SQLException {
        String sql = """
            INSERT INTO fechamento_caixa
                (data, data_hora_fechamento, funcionario_id, nome_funcionario,
                 dinheiro_esperado, dinheiro_contado, pixdebito_esperado, pixdebito_contado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, data);
            comando.setString(2, dataHoraFechamento);
            if (funcionarioId == null) comando.setNull(3, java.sql.Types.INTEGER);
            else comando.setInt(3, funcionarioId);
            comando.setString(4, nomeFuncionario);
            comando.setDouble(5, dinheiroEsperado);
            comando.setDouble(6, dinheiroContado);
            comando.setDouble(7, pixdebitoEsperado);
            comando.setDouble(8, pixdebitoContado);
            comando.executeUpdate();
        }
    }

    public List<FechamentoCaixa> listarDoMes(String mes) throws SQLException { // fechamentos de um mês (aaaa-mm)
        String sql = """
            SELECT data, data_hora_fechamento, nome_funcionario,
                   dinheiro_esperado, dinheiro_contado, pixdebito_esperado, pixdebito_contado
            FROM fechamento_caixa
            WHERE strftime('%Y-%m', data) = ?
            ORDER BY data_hora_fechamento DESC
            """;
        List<FechamentoCaixa> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, mes);
            try (ResultSet r = comando.executeQuery()) {
                while (r.next()) {
                    FechamentoCaixa f = new FechamentoCaixa();
                    f.setData(r.getString("data"));
                    f.setDataHoraFechamento(r.getString("data_hora_fechamento"));
                    f.setNomeFuncionario(r.getString("nome_funcionario"));
                    f.setDinheiroEsperado(r.getDouble("dinheiro_esperado"));
                    f.setDinheiroContado(r.getDouble("dinheiro_contado"));
                    f.setPixdebitoEsperado(r.getDouble("pixdebito_esperado"));
                    f.setPixdebitoContado(r.getDouble("pixdebito_contado"));
                    lista.add(f);
                }
            }
        }
        return lista;
    }
}