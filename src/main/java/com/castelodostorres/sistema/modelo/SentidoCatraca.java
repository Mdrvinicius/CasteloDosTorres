package com.castelodostorres.sistema.modelo;

public enum SentidoCatraca { // ENUM: conjunto fixo de valores possíveis, não é class comum

    HORARIO("clockwise", "Sentido Horário"),        // uma das 2 opções possíveis
    ANTI_HORARIO("anticlockwise", "Sentido Anti-Horário"); // a outra opção possível

    private final String valorApi;  // ATRIBUTO: o texto exato que a Access API espera (allow=clockwise/anticlockwise)
    private final String rotulo;    // ATRIBUTO: texto amigável pra mostrar na tela/log

    SentidoCatraca(String valorApi, String rotulo) { // CONSTRUTOR: roda pra cada uma das 2 opções acima, na criação
        this.valorApi = valorApi;
        this.rotulo = rotulo;
    }

    public String getValorApi() { // MÉTODO: devolve o valor pro endpoint da catraca
        return valorApi;
    }

    public String getRotulo() {   // MÉTODO: devolve o texto amigável
        return rotulo;
    }
}