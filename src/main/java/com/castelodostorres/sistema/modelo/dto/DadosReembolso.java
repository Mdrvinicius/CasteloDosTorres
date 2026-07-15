package com.castelodostorres.sistema.modelo.dto;

public class DadosReembolso {

    private final double valor;   // ATRIBUTO: quanto reembolsar
    private final String motivo;  // ATRIBUTO: por que reembolsar

    public DadosReembolso(double valor, String motivo) { // CONSTRUTOR
        this.valor = valor;
        this.motivo = motivo;
    }

    public double getValor() { return valor; }
    public String getMotivo() { return motivo; }
}