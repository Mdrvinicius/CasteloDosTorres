package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.ItemVenda;
import com.castelodostorres.sistema.modelo.Venda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VendaRepositorio {

    public void salvar(Venda venda) throws SQLException { // MÉTODO: grava venda + itens + baixa estoque numa TRANSAÇÃO (tudo-ou-nada)
        Connection conexao = GerenciadorConexao.getConexao();
        boolean autoCommitAntigo = conexao.getAutoCommit();

        try {
            conexao.setAutoCommit(false);

            String sqlVenda = """
                INSERT INTO venda (data_hora, valor_total, valor_dinheiro, valor_pix, valor_debito, status)
                VALUES (?, ?, ?, ?, ?, 'ATIVA')
                """;
            int vendaId;
            try (PreparedStatement cmd = conexao.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                cmd.setString(1, venda.getDataHora());
                cmd.setDouble(2, venda.getValorTotal());
                cmd.setDouble(3, venda.getValorDinheiro());
                cmd.setDouble(4, venda.getValorPix());
                cmd.setDouble(5, venda.getValorDebito());
                cmd.executeUpdate();
                try (ResultSet chaves = cmd.getGeneratedKeys()) {
                    if (chaves.next()) vendaId = chaves.getInt(1);
                    else throw new SQLException("Falha ao gerar id da venda.");
                }
            }
            venda.setId(vendaId);

            String sqlItem = """
                INSERT INTO item_venda (venda_id, produto_id, nome_produto, quantidade, preco_venda_unitario, preco_custo_unitario)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            String sqlBaixa = "UPDATE produto SET estoque = estoque - ? WHERE id = ?";

            try (PreparedStatement cmdItem = conexao.prepareStatement(sqlItem);
                 PreparedStatement cmdBaixa = conexao.prepareStatement(sqlBaixa)) {

                for (ItemVenda item : venda.getItens()) {
                    cmdItem.setInt(1, vendaId);
                    cmdItem.setInt(2, item.getProdutoId());
                    cmdItem.setString(3, item.getNomeProduto());
                    cmdItem.setInt(4, item.getQuantidade());
                    cmdItem.setDouble(5, item.getPrecoVendaUnitario());
                    cmdItem.setDouble(6, item.getPrecoCustoUnitario());
                    cmdItem.executeUpdate();

                    cmdBaixa.setInt(1, item.getQuantidade());
                    cmdBaixa.setInt(2, item.getProdutoId());
                    cmdBaixa.executeUpdate();
                }
            }

            conexao.commit();
        } catch (SQLException e) {
            conexao.rollback();
            throw e;
        } finally {
            conexao.setAutoCommit(autoCommitAntigo);
        }
    }

    public void cancelar(int vendaId, String motivo) throws SQLException { // MÉTODO: cancela venda e DEVOLVE estoque (transação)
        Connection conexao = GerenciadorConexao.getConexao();
        boolean autoCommitAntigo = conexao.getAutoCommit();

        try {
            conexao.setAutoCommit(false);

            String sqlItens = "SELECT produto_id, quantidade FROM item_venda WHERE venda_id = ?";
            List<int[]> itens = new ArrayList<>();
            try (PreparedStatement cmd = conexao.prepareStatement(sqlItens)) {
                cmd.setInt(1, vendaId);
                try (ResultSet r = cmd.executeQuery()) {
                    while (r.next()) {
                        itens.add(new int[]{ r.getInt("produto_id"), r.getInt("quantidade") });
                    }
                }
            }

            String sqlDevolve = "UPDATE produto SET estoque = estoque + ? WHERE id = ?";
            try (PreparedStatement cmd = conexao.prepareStatement(sqlDevolve)) {
                for (int[] item : itens) {
                    cmd.setInt(1, item[1]);
                    cmd.setInt(2, item[0]);
                    cmd.executeUpdate();
                }
            }

            String sqlCancelar = """
                UPDATE venda
                SET status = 'CANCELADA', motivo_cancelamento = ?, data_hora_cancelamento = ?
                WHERE id = ?
                """;
            try (PreparedStatement cmd = conexao.prepareStatement(sqlCancelar)) {
                cmd.setString(1, motivo);
                cmd.setString(2, java.time.LocalDateTime.now().toString());
                cmd.setInt(3, vendaId);
                cmd.executeUpdate();
            }

            conexao.commit();
        } catch (SQLException e) {
            conexao.rollback();
            throw e;
        } finally {
            conexao.setAutoCommit(autoCommitAntigo);
        }
    }

    public double calcularArrecadadoDoDia(String data) throws SQLException {
        String sql = """
        SELECT COALESCE(SUM(valor_total), 0) AS total
        FROM venda
        WHERE date(data_hora) = ?
          AND status != 'CANCELADA'
        """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setString(1, data);
            try (ResultSet r = cmd.executeQuery()) {
                if (r.next()) return r.getDouble("total");
            }
        }
        return 0.0;
    }

    public double calcularArrecadadoDoMes(String mes) throws SQLException {
        String sql = """
        SELECT COALESCE(SUM(valor_total), 0) AS total
        FROM venda
        WHERE strftime('%Y-%m', data_hora) = ?
          AND status != 'CANCELADA'
        """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setString(1, mes);
            try (ResultSet r = cmd.executeQuery()) {
                if (r.next()) return r.getDouble("total");
            }
        }
        return 0.0;
    }

    public double[] calcularFormasPagamentoDoDia(String data) throws SQLException {
        String sql = """
        SELECT
            COALESCE(SUM(valor_dinheiro), 0) AS dinheiro,
            COALESCE(SUM(valor_pix), 0) AS pix,
            COALESCE(SUM(valor_debito), 0) AS debito
        FROM venda
        WHERE date(data_hora) = ?
          AND status != 'CANCELADA'
        """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setString(1, data);
            try (ResultSet r = cmd.executeQuery()) {
                if (r.next()) {
                    return new double[] { r.getDouble("dinheiro"), r.getDouble("pix"), r.getDouble("debito") };
                }
            }
        }
        return new double[] { 0, 0, 0 };
    }

    public double calcularCustoLojaDoMes(String mes) throws SQLException {
        String sql = """
        SELECT COALESCE(SUM(iv.quantidade * iv.preco_custo_unitario), 0) AS custo
        FROM item_venda iv
        JOIN venda v ON iv.venda_id = v.id
        WHERE strftime('%Y-%m', v.data_hora) = ?
          AND v.status != 'CANCELADA'
        """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setString(1, mes);
            try (ResultSet r = cmd.executeQuery()) {
                if (r.next()) return r.getDouble("custo");
            }
        }
        return 0.0;
    }

    // ===== MÉTODOS NOVOS (relatório de vendas) =====

    public double calcularCustoLojaDoDia(String data) throws SQLException { // MÉTODO: custo dos produtos vendidos no dia
        String sql = """
        SELECT COALESCE(SUM(iv.quantidade * iv.preco_custo_unitario), 0) AS custo
        FROM item_venda iv
        JOIN venda v ON iv.venda_id = v.id
        WHERE date(v.data_hora) = ?
          AND v.status != 'CANCELADA'
        """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setString(1, data);
            try (ResultSet r = cmd.executeQuery()) {
                if (r.next()) return r.getDouble("custo");
            }
        }
        return 0.0;
    }

    public List<Venda> listarDoDia(String data) throws SQLException { // MÉTODO: vendas de um dia (todas, inclusive canceladas, pra mostrar status)
        String sql = "SELECT * FROM venda WHERE date(data_hora) = ? ORDER BY data_hora DESC";
        return executarLista(sql, data);
    }

    public List<Venda> listarDoMes(String mes) throws SQLException { // MÉTODO: vendas de um mês
        String sql = "SELECT * FROM venda WHERE strftime('%Y-%m', data_hora) = ? ORDER BY data_hora DESC";
        return executarLista(sql, mes);
    }

    public List<ItemVenda> listarItens(int vendaId) throws SQLException { // MÉTODO: itens de uma venda
        String sql = "SELECT * FROM item_venda WHERE venda_id = ?";
        List<ItemVenda> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setInt(1, vendaId);
            try (ResultSet r = cmd.executeQuery()) {
                while (r.next()) {
                    ItemVenda iv = new ItemVenda();
                    iv.setId(r.getInt("id"));
                    iv.setVendaId(r.getInt("venda_id"));
                    iv.setProdutoId(r.getInt("produto_id"));
                    iv.setNomeProduto(r.getString("nome_produto"));
                    iv.setQuantidade(r.getInt("quantidade"));
                    iv.setPrecoVendaUnitario(r.getDouble("preco_venda_unitario"));
                    iv.setPrecoCustoUnitario(r.getDouble("preco_custo_unitario"));
                    lista.add(iv);
                }
            }
        }
        return lista;
    }

    private List<Venda> executarLista(String sql, String parametro) throws SQLException { // auxiliar: monta lista de vendas
        List<Venda> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setString(1, parametro);
            try (ResultSet r = cmd.executeQuery()) {
                while (r.next()) {
                    Venda v = new Venda();
                    v.setId(r.getInt("id"));
                    v.setDataHora(r.getString("data_hora"));
                    v.setValorTotal(r.getDouble("valor_total"));
                    v.setValorDinheiro(r.getDouble("valor_dinheiro"));
                    v.setValorPix(r.getDouble("valor_pix"));
                    v.setValorDebito(r.getDouble("valor_debito"));
                    v.setStatus(r.getString("status"));
                    v.setMotivoCancelamento(r.getString("motivo_cancelamento"));
                    lista.add(v);
                }
            }
        }
        return lista;
    }
}