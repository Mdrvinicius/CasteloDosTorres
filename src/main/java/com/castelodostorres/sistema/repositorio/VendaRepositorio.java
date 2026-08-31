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
            conexao.setAutoCommit(false); // inicia a transação

            // 1) grava o cabeçalho da venda
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

            // 2) grava cada item + 3) dá baixa no estoque
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

            conexao.commit(); // tudo deu certo: confirma
        } catch (SQLException e) {
            conexao.rollback(); // deu erro no meio: desfaz tudo
            throw e;
        } finally {
            conexao.setAutoCommit(autoCommitAntigo); // restaura o modo normal
        }
    }

    public void cancelar(int vendaId, String motivo) throws SQLException { // MÉTODO: cancela venda e DEVOLVE estoque (transação)
        Connection conexao = GerenciadorConexao.getConexao();
        boolean autoCommitAntigo = conexao.getAutoCommit();

        try {
            conexao.setAutoCommit(false);

            // devolve o estoque de cada item da venda
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
                    cmd.setInt(1, item[1]); // quantidade
                    cmd.setInt(2, item[0]); // produto_id
                    cmd.executeUpdate();
                }
            }

            // marca a venda como cancelada
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
}