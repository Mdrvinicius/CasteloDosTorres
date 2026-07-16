package com.castelodostorres.sistema.modelo.dto;

public class ComissaoFuncionario {

    private final String nome;    // ATRIBUTO: nome do funcionário
    private final String papel;   // ATRIBUTO: "GUIA" ou "RECEPCIONISTA"
    private double valor;         // ATRIBUTO: total acumulado a receber (não é final: vai somando)

    public ComissaoFuncionario(String nome, String papel, double valor) { // CONSTRUTOR
        this.nome = nome;
        this.papel = papel;
        this.valor = valor;
    }

    public String getNome() { return nome; }
    public String getPapel() { return papel; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public void adicionar(double valor) { this.valor += valor; } // MÉTODO: soma ao acumulado
}