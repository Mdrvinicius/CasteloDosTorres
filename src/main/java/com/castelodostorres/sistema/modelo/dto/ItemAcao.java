package com.castelodostorres.sistema.modelo.dto;

public class ItemAcao { // representa UM item da lista "actions": {"action": "catra", "parameters": "allow=..."}

    private String action;      // ATRIBUTO: sempre vai ser "catra" no nosso caso
    private String parameters;  // ATRIBUTO: vai ser "allow=clockwise" ou "allow=anticlockwise"

    public ItemAcao() { // CONSTRUTOR vazio: necessário pro Jackson (mesma razão de RespostaLogin)
    }

    public ItemAcao(String action, String parameters) { // CONSTRUTOR com dados: o que NÓS vamos usar
        this.action = action;
        this.parameters = parameters;
    }

    public String getAction() {        // MÉTODO
        return action;
    }

    public void setAction(String action) {  // MÉTODO
        this.action = action;
    }

    public String getParameters() {    // MÉTODO
        return parameters;
    }

    public void setParameters(String parameters) {  // MÉTODO
        this.parameters = parameters;
    }
}