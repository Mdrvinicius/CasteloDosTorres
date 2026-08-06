package com.castelodostorres.sistema.modelo;

public class FechamentoCaixa {

    private String data;                  // ATRIBUTO: dia fechado
    private String dataHoraFechamento;    // ATRIBUTO
    private String nomeFuncionario;       // ATRIBUTO
    private double dinheiroEsperado;      // ATRIBUTO
    private double dinheiroContado;       // ATRIBUTO
    private double pixdebitoEsperado;     // ATRIBUTO
    private double pixdebitoContado;      // ATRIBUTO

    private double entregue;              // ATRIBUTO: dinheiro entregue ao dono no fechamento
    private double emCaixa;               // ATRIBUTO: dinheiro que fica pra o dia seguinte (vira o fundo herdado)
    private double fundoHerdado;          // ATRIBUTO: "em caixa" que o dia anterior deixou (esperado na abertura)
    private double fundoReal;             // ATRIBUTO: fundo que de fato abriu o dia (o que a recepcionista confirmou)
    private boolean temFundoHerdado;      // ATRIBUTO: true se houve herança do dia anterior; false = fundo manual

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

    public double getEntregue() { return entregue; }
    public void setEntregue(double v) { this.entregue = v; }

    public double getEmCaixa() { return emCaixa; }
    public void setEmCaixa(double v) { this.emCaixa = v; }

    public double getFundoHerdado() { return fundoHerdado; }
    public void setFundoHerdado(double v) { this.fundoHerdado = v; }

    public double getFundoReal() { return fundoReal; }
    public void setFundoReal(double v) { this.fundoReal = v; }

    public boolean isTemFundoHerdado() { return temFundoHerdado; }
    public void setTemFundoHerdado(boolean v) { this.temFundoHerdado = v; }

    // divergências calculadas (não vêm do banco)
    public double getDivergenciaDinheiro() { return dinheiroContado - dinheiroEsperado; }
    public double getDivergenciaPixdebito() { return pixdebitoContado - pixdebitoEsperado; }

    // divergência do fundo herdado (só faz sentido quando temFundoHerdado)
    public double getDivergenciaFundo() { return fundoReal - fundoHerdado; }

    // status do fundo, pronto pra exibir na tabela do mês
    public String getStatusFundo() { // MÉTODO: texto do status do fundo herdado
        if (!temFundoHerdado) {
            return "Fundo iniciado manualmente";
        }
        double dif = getDivergenciaFundo();
        if (Math.abs(dif) < 0.001) {
            return "Fundo de troco correto";
        }
        return dif < 0
                ? "Faltou R$ " + String.format("%.2f", -dif) + " no fundo"
                : "Sobrou R$ " + String.format("%.2f", dif) + " no fundo";
    }
}