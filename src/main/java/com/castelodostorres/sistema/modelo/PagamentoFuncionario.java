package com.castelodostorres.sistema.modelo;

public class PagamentoFuncionario {

    private Integer id;                  // ATRIBUTO
    private int funcionarioId;           // ATRIBUTO: FK pro funcionário
    private String nomeFuncionario;      // ATRIBUTO: snapshot do nome
    private String mesReferencia;        // ATRIBUTO: "aaaa-mm" (mês da comissão paga)
    private double valor;                // ATRIBUTO
    private String dataHoraRegistro;     // ATRIBUTO: quando foi registrado

    public PagamentoFuncionario() { // CONSTRUTOR vazio
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(int funcionarioId) { this.funcionarioId = funcionarioId; }

    public String getNomeFuncionario() { return nomeFuncionario; }
    public void setNomeFuncionario(String nomeFuncionario) { this.nomeFuncionario = nomeFuncionario; }

    public String getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(String mesReferencia) { this.mesReferencia = mesReferencia; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getDataHoraRegistro() { return dataHoraRegistro; }
    public void setDataHoraRegistro(String dataHoraRegistro) { this.dataHoraRegistro = dataHoraRegistro; }
}