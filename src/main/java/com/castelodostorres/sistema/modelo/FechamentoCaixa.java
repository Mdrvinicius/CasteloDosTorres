package com.castelodostorres.sistema.modelo;

public class FechamentoCaixa {

    private String data;                  // ATRIBUTO: dia fechado
    private String dataHoraFechamento;    // ATRIBUTO
    private String nomeFuncionario;       // ATRIBUTO
    private double dinheiroEsperado;      // ATRIBUTO
    private double dinheiroContado;       // ATRIBUTO
    private double pixdebitoEsperado;     // ATRIBUTO
    private double pixdebitoContado;      // ATRIBUTO

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getDataHoraFechamento() { return dataHoraFechamento; }
    public void setDataHoraFechamento(String v) { this.dataHoraFechamento = v; }

    public String getNomeFuncionario() { return nomeFuncionario; }
    public void setNomeFuncionario(String v) { this.nomeFuncionario = v; }

    public double getDinheiroEsperado() { return dinheiroEsperado; }
    public void setDinheiroEsperado(double v) { this.dinheiroEsperado = v; }

    public double getDinheiroContado() { return dinheiroContado; }
    public void setDinheiroContado(double v) { this.dinheiroContado = v; }

    public double getPixdebitoEsperado() { return pixdebitoEsperado; }
    public void setPixdebitoEsperado(double v) { this.pixdebitoEsperado = v; }

    public double getPixdebitoContado() { return pixdebitoContado; }
    public void setPixdebitoContado(double v) { this.pixdebitoContado = v; }

    // divergências calculadas (não vêm do banco)
    public double getDivergenciaDinheiro() { return dinheiroContado - dinheiroEsperado; }
    public double getDivergenciaPixdebito() { return pixdebitoContado - pixdebitoEsperado; }
}