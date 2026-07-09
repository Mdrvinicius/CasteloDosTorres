package com.castelodostorres.sistema.servico;

import java.net.http.HttpClient;

public class ServicoControlId {

    private final HttpClient httpClient;

    public ServicoControlId() {
        this.httpClient = HttpClient.newHttpClient();
    }

}