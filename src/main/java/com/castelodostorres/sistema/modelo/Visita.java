package com.castelodostorres.sistema.modelo;

public class Visita {

    private Integer id;                              // ATRIBUTO: id da visita (null antes de salvar, banco gera)
    private int guiaId;                              // ATRIBUTO: id do guia (obrigatório, sempre tem)
    private Integer recepcionistaId;                 // ATRIBUTO: id da recepcionista (Integer pra poder ser null = "Nenhuma")

    private String guiaTipoRemuneracao;              // ATRIBUTO: snapshot do tipo de remuneração do guia
    private double guiaValorRemuneracao;             // ATRIBUTO: snapshot do valor de remuneração do guia
    private String recepcionistaTipoRemuneracao;     // ATRIBUTO: snapshot (null se não tem recepcionista)
    private Double recepcionistaValorRemuneracao;    // ATRIBUTO: snapshot (Double pra poder ser null)

    private int quantidadeInteira;                   // ATRIBUTO: contador
    private int quantidadeMeia;                      // ATRIBUTO: contador
    private int quantidadeNaoPagante;                // ATRIBUTO: contador

    private double valorUnitarioInteira;             // ATRIBUTO: snapshot do preço da inteira
    private double valorUnitarioMeia;                // ATRIBUTO: snapshot do preço da meia
    private double valorTotal;                       // ATRIBUTO: valor total calculado

    private String observacoes;                      // ATRIBUTO: justificativa de meia-entrada (texto livre)
    private String dataHoraInicio;                   // ATRIBUTO: quando a visita começou
    private String status;                           // ATRIBUTO: "ATIVA" ou "CANCELADA"

    private String nomeGuia; // ATRIBUTO: nome da guia, vindo do JOIN (não é coluna da tabela visita, só pra exibição)

    private double valorReembolsado;   // ATRIBUTO: quanto foi devolvido (0 se não houve reembolso)
    private String motivoReembolso;    // ATRIBUTO: justificativa do reembolso (null se não houve)
    private double valorDinheiro;      // ATRIBUTO: parte do total paga em dinheiro
    private double valorPix;           // ATRIBUTO: parte paga em pix
    private double valorDebito;        // ATRIBUTO: parte paga em débito

    private String motivoCancelamento;

    private boolean agendada; // ATRIBUTO: true = paga antecipada ao dono, fora da conferência de caixa

    public Visita() { // CONSTRUTOR vazio
    }

    public double getValorReembolsado() { return valorReembolsado; }
    public void setValorReembolsado(double valorReembolsado) { this.valorReembolsado = valorReembolsado; }

    public String getMotivoReembolso() { return motivoReembolso; }
    public void setMotivoReembolso(String motivoReembolso) { this.motivoReembolso = motivoReembolso; }

    public double getValorDinheiro() { return valorDinheiro; }
    public void setValorDinheiro(double valorDinheiro) { this.valorDinheiro = valorDinheiro; }

    public double getValorPix() { return valorPix; }
    public void setValorPix(double valorPix) { this.valorPix = valorPix; }

    public double getValorDebito() { return valorDebito; }
    public void setValorDebito(double valorDebito) { this.valorDebito = valorDebito; }

    public String getNomeGuia() { return nomeGuia; }
    public void setNomeGuia(String nomeGuia) { this.nomeGuia = nomeGuia; }

    private String nomeRecepcionista; // ATRIBUTO: nome da recepcionista via JOIN (null se "Nenhuma"), só pra exibição

    public String getNomeRecepcionista() { return nomeRecepcionista; }
    public void setNomeRecepcionista(String nomeRecepcionista) { this.nomeRecepcionista = nomeRecepcionista; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getGuiaId() { return guiaId; }
    public void setGuiaId(int guiaId) { this.guiaId = guiaId; }

    public Integer getRecepcionistaId() { return recepcionistaId; }
    public void setRecepcionistaId(Integer recepcionistaId) { this.recepcionistaId = recepcionistaId; }

    public String getGuiaTipoRemuneracao() { return guiaTipoRemuneracao; }
    public void setGuiaTipoRemuneracao(String guiaTipoRemuneracao) { this.guiaTipoRemuneracao = guiaTipoRemuneracao; }

    public double getGuiaValorRemuneracao() { return guiaValorRemuneracao; }
    public void setGuiaValorRemuneracao(double guiaValorRemuneracao) { this.guiaValorRemuneracao = guiaValorRemuneracao; }

    public String getRecepcionistaTipoRemuneracao() { return recepcionistaTipoRemuneracao; }
    public void setRecepcionistaTipoRemuneracao(String recepcionistaTipoRemuneracao) { this.recepcionistaTipoRemuneracao = recepcionistaTipoRemuneracao; }

    public Double getRecepcionistaValorRemuneracao() { return recepcionistaValorRemuneracao; }
    public void setRecepcionistaValorRemuneracao(Double recepcionistaValorRemuneracao) { this.recepcionistaValorRemuneracao = recepcionistaValorRemuneracao; }

    public int getQuantidadeInteira() { return quantidadeInteira; }
    public void setQuantidadeInteira(int quantidadeInteira) { this.quantidadeInteira = quantidadeInteira; }

    public int getQuantidadeMeia() { return quantidadeMeia; }
    public void setQuantidadeMeia(int quantidadeMeia) { this.quantidadeMeia = quantidadeMeia; }

    public int getQuantidadeNaoPagante() { return quantidadeNaoPagante; }
    public void setQuantidadeNaoPagante(int quantidadeNaoPagante) { this.quantidadeNaoPagante = quantidadeNaoPagante; }

    public double getValorUnitarioInteira() { return valorUnitarioInteira; }
    public void setValorUnitarioInteira(double valorUnitarioInteira) { this.valorUnitarioInteira = valorUnitarioInteira; }

    public double getValorUnitarioMeia() { return valorUnitarioMeia; }
    public void setValorUnitarioMeia(double valorUnitarioMeia) { this.valorUnitarioMeia = valorUnitarioMeia; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(String dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public boolean isAgendada() { return agendada; }
    public void setAgendada(boolean agendada) { this.agendada = agendada; }



}