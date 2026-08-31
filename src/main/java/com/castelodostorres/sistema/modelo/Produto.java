package com.castelodostorres.sistema.modelo;

public class Produto {

    private Integer id;              // ATRIBUTO: id (null antes de salvar, banco gera)
    private String nome;             // ATRIBUTO
    private String categoria;        // ATRIBUTO: texto livre (pode ser null)
    private double precoVenda;       // ATRIBUTO
    private double precoCusto;       // ATRIBUTO
    private int estoque;             // ATRIBUTO: quantidade atual
    private String imagem;           // ATRIBUTO: caminho/nome do arquivo (null se sem imagem)
    private boolean ativo;           // ATRIBUTO: soft delete (false = excluído)

    public Produto() { // CONSTRUTOR vazio
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }

    public double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    public String getImagem() { return imagem; }
    public void setImagem(String imagem) { this.imagem = imagem; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}