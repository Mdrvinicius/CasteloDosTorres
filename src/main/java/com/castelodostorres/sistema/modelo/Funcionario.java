package com.castelodostorres.sistema.modelo;

public class Funcionario {

    private Integer id;
    private String nome;
    private String papel;
    private String tipoRemuneracao;
    private double valorRemuneracao;

    public Funcionario() {
    }

    public Funcionario(String nome, String papel, String tipoRemuneracao, double valorRemuneracao) {
        this.nome = nome;
        this.papel = papel;
        this.tipoRemuneracao = tipoRemuneracao;
        this.valorRemuneracao = valorRemuneracao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }

    public String getTipoRemuneracao() {
        return tipoRemuneracao;
    }

    public void setTipoRemuneracao(String tipoRemuneracao) {
        this.tipoRemuneracao = tipoRemuneracao;
    }

    public double getValorRemuneracao() {
        return valorRemuneracao;
    }

    public void setValorRemuneracao(double valorRemuneracao) {
        this.valorRemuneracao = valorRemuneracao;
    }
}