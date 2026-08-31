package com.castelodostorres.sistema.modelo;

public class ItemVenda {

    private Integer id;                   // ATRIBUTO
    private Integer vendaId;              // ATRIBUTO: FK pra venda
    private Integer produtoId;            // ATRIBUTO: FK pra produto
    private String nomeProduto;           // ATRIBUTO: snapshot do nome na hora da venda
    private int quantidade;               // ATRIBUTO
    private double precoVendaUnitario;    // ATRIBUTO: snapshot do preço de venda
    private double precoCustoUnitario;    // ATRIBUTO: snapshot do preço de custo

    public ItemVenda() { // CONSTRUTOR vazio
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVendaId() { return vendaId; }
    public void setVendaId(Integer vendaId) { this.vendaId = vendaId; }

    public Integer getProdutoId() { return produtoId; }
    public void setProdutoId(Integer produtoId) { this.produtoId = produtoId; }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getPrecoVendaUnitario() { return precoVendaUnitario; }
    public void setPrecoVendaUnitario(double precoVendaUnitario) { this.precoVendaUnitario = precoVendaUnitario; }

    public double getPrecoCustoUnitario() { return precoCustoUnitario; }
    public void setPrecoCustoUnitario(double precoCustoUnitario) { this.precoCustoUnitario = precoCustoUnitario; }

    // subtotais calculados (não vêm do banco)
    public double getSubtotalVenda() { return quantidade * precoVendaUnitario; }
    public double getSubtotalCusto() { return quantidade * precoCustoUnitario; }
}