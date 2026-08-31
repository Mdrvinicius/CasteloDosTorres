package com.castelodostorres.sistema.modelo;

import java.util.ArrayList;
import java.util.List;

public class Venda {

    private Integer id;                  // ATRIBUTO
    private String dataHora;             // ATRIBUTO
    private double valorTotal;           // ATRIBUTO
    private double valorDinheiro;        // ATRIBUTO
    private double valorPix;             // ATRIBUTO
    private double valorDebito;          // ATRIBUTO
    private String status;               // ATRIBUTO: "ATIVA" ou "CANCELADA"
    private String motivoCancelamento;   // ATRIBUTO: null se não cancelada

    private List<ItemVenda> itens = new ArrayList<>(); // ATRIBUTO: os produtos desta venda (carrinho)

    public Venda() { // CONSTRUTOR vazio
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public double getValorDinheiro() { return valorDinheiro; }
    public void setValorDinheiro(double valorDinheiro) { this.valorDinheiro = valorDinheiro; }

    public double getValorPix() { return valorPix; }
    public void setValorPix(double valorPix) { this.valorPix = valorPix; }

    public double getValorDebito() { return valorDebito; }
    public void setValorDebito(double valorDebito) { this.valorDebito = valorDebito; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public List<ItemVenda> getItens() { return itens; }
    public void setItens(List<ItemVenda> itens) { this.itens = itens; }
    public void adicionarItem(ItemVenda item) { this.itens.add(item); }
}