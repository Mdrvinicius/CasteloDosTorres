package com.castelodostorres.sistema.modelo.dto;

import java.util.List;

public class RequisicaoExecutarAcoes { // representa o objeto de fora: {"actions": [ ... ]}

    private List<ItemAcao> actions; // ATRIBUTO: uma LISTA de ItemAcao (no nosso caso, sempre com 1 item só)

    public RequisicaoExecutarAcoes() { // CONSTRUTOR vazio: pro Jackson
    }

    public RequisicaoExecutarAcoes(List<ItemAcao> actions) { // CONSTRUTOR com dados: o que NÓS vamos usar
        this.actions = actions;
    }

    public List<ItemAcao> getActions() {  // MÉTODO
        return actions;
    }

    public void setActions(List<ItemAcao> actions) {  // MÉTODO
        this.actions = actions;
    }
}