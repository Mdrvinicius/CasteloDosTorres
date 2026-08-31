package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepositorio {

    public void salvar(Produto produto) throws SQLException { // MÉTODO: insere um novo produto
        String sql = """
            INSERT INTO produto (nome, categoria, preco_venda, preco_custo, estoque, imagem, ativo)
            VALUES (?, ?, ?, ?, ?, ?, 1)
            """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, produto.getNome());
            if (produto.getCategoria() == null) comando.setNull(2, Types.VARCHAR);
            else comando.setString(2, produto.getCategoria());
            comando.setDouble(3, produto.getPrecoVenda());
            comando.setDouble(4, produto.getPrecoCusto());
            comando.setInt(5, produto.getEstoque());
            if (produto.getImagem() == null) comando.setNull(6, Types.VARCHAR);
            else comando.setString(6, produto.getImagem());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) produto.setId(chaves.getInt(1));
            }
        }
    }

    public void atualizar(Produto produto) throws SQLException { // MÉTODO: atualiza um produto existente
        String sql = """
            UPDATE produto
            SET nome = ?, categoria = ?, preco_venda = ?, preco_custo = ?, estoque = ?, imagem = ?
            WHERE id = ?
            """;
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, produto.getNome());
            if (produto.getCategoria() == null) comando.setNull(2, Types.VARCHAR);
            else comando.setString(2, produto.getCategoria());
            comando.setDouble(3, produto.getPrecoVenda());
            comando.setDouble(4, produto.getPrecoCusto());
            comando.setInt(5, produto.getEstoque());
            if (produto.getImagem() == null) comando.setNull(6, Types.VARCHAR);
            else comando.setString(6, produto.getImagem());
            comando.setInt(7, produto.getId());
            comando.executeUpdate();
        }
    }

    public void desativar(int produtoId) throws SQLException { // MÉTODO: soft delete (some da loja, mas histórico de vendas fica)
        String sql = "UPDATE produto SET ativo = 0 WHERE id = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, produtoId);
            comando.executeUpdate();
        }
    }

    public List<Produto> listarAtivos() throws SQLException { // MÉTODO: produtos ativos (pra vitrine e lista)
        String sql = "SELECT * FROM produto WHERE ativo = 1 ORDER BY nome";
        return executarLista(sql);
    }

    public List<Produto> buscarAtivos(String termo, String categoria) throws SQLException { // MÉTODO: filtro por nome e/ou categoria (só ativos)
        StringBuilder sql = new StringBuilder("SELECT * FROM produto WHERE ativo = 1 ");
        if (termo != null && !termo.isBlank()) {
            sql.append(" AND nome LIKE ? ");
        }
        if (categoria != null && !categoria.isBlank()) {
            sql.append(" AND categoria = ? ");
        }
        sql.append(" ORDER BY nome ");

        List<Produto> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql.toString())) {
            int indice = 1;
            if (termo != null && !termo.isBlank()) {
                comando.setString(indice++, "%" + termo + "%");
            }
            if (categoria != null && !categoria.isBlank()) {
                comando.setString(indice++, categoria);
            }
            try (ResultSet r = comando.executeQuery()) {
                while (r.next()) lista.add(mapear(r));
            }
        }
        return lista;
    }

    public Produto buscarPorId(int id) throws SQLException { // MÉTODO: um produto específico (pra vender/dar baixa)
        String sql = "SELECT * FROM produto WHERE id = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet r = comando.executeQuery()) {
                if (r.next()) return mapear(r);
            }
        }
        return null;
    }

    public List<String> listarCategorias() throws SQLException { // MÉTODO: categorias distintas existentes (pro filtro)
        String sql = "SELECT DISTINCT categoria FROM produto WHERE ativo = 1 AND categoria IS NOT NULL AND categoria != '' ORDER BY categoria";
        List<String> categorias = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet r = comando.executeQuery()) {
            while (r.next()) categorias.add(r.getString("categoria"));
        }
        return categorias;
    }

    public void baixarEstoque(int produtoId, int quantidade) throws SQLException { // MÉTODO: reduz o estoque (usado na venda)
        String sql = "UPDATE produto SET estoque = estoque - ? WHERE id = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, quantidade);
            comando.setInt(2, produtoId);
            comando.executeUpdate();
        }
    }

    public void devolverEstoque(int produtoId, int quantidade) throws SQLException { // MÉTODO: repõe o estoque (usado no cancelamento de venda)
        String sql = "UPDATE produto SET estoque = estoque + ? WHERE id = ?";
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, quantidade);
            comando.setInt(2, produtoId);
            comando.executeUpdate();
        }
    }

    private List<Produto> executarLista(String sql) throws SQLException { // MÉTODO auxiliar: roda um SELECT sem parâmetros e mapeia
        List<Produto> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet r = comando.executeQuery()) {
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    private Produto mapear(ResultSet r) throws SQLException { // MÉTODO auxiliar: converte uma linha do ResultSet num Produto
        Produto p = new Produto();
        p.setId(r.getInt("id"));
        p.setNome(r.getString("nome"));
        p.setCategoria(r.getString("categoria"));
        p.setPrecoVenda(r.getDouble("preco_venda"));
        p.setPrecoCusto(r.getDouble("preco_custo"));
        p.setEstoque(r.getInt("estoque"));
        p.setImagem(r.getString("imagem"));
        p.setAtivo(r.getInt("ativo") == 1);
        return p;
    }
}