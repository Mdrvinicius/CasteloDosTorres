package com.castelodostorres.sistema.modelo;

public class Despesa {

    private Integer id;                  // ATRIBUTO
    private String nome;                 // ATRIBUTO
    private double valor;                // ATRIBUTO
    private String tipo;                 // ATRIBUTO: "AVULSA" ou "RECORRENTE"
    private String dataHoraCadastro;     // ATRIBUTO

    public Despesa() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDataHoraCadastro() { return dataHoraCadastro; }
    public void setDataHoraCadastro(String dataHoraCadastro) { this.dataHoraCadastro = dataHoraCadastro; }
}