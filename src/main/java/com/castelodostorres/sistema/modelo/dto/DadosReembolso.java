package com.castelodostorres.sistema.modelo.dto;

public class DadosReembolso {

    private final double valor;   // ATRIBUTO: quanto reembolsar
    private final String motivo;  // ATRIBUTO: por que reembolsar
    private final String forma;

    public DadosReembolso(double valor, String motivo, String forma) { // CONSTRUTOR
        this.valor = valor;
        this.motivo = motivo;
        this.forma = forma;
    }

    public double getValor() { return valor; }
    public String getMotivo() { return motivo; }
    public String getForma() { return forma; }
}