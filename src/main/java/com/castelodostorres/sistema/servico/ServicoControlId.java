package com.castelodostorres.sistema.servico;

import com.castelodostorres.sistema.modelo.SentidoCatraca;
import com.castelodostorres.sistema.modelo.dto.ItemAcao;
import com.castelodostorres.sistema.modelo.dto.RequisicaoExecutarAcoes;
import com.castelodostorres.sistema.modelo.dto.RequisicaoLogin;
import com.castelodostorres.sistema.modelo.dto.RespostaLogin;
import com.castelodostorres.sistema.util.ProvedorMapeadorJson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ServicoControlId {

    private final HttpClient httpClient;
    private String sessionToken;
    private String baseUrl; // ATRIBUTO NOVO: guarda "http://IP:porta" após o login, pra reusar depois

    public ServicoControlId() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public void login(String ip, int porta, String usuario, String senha) throws Exception {
        this.baseUrl = "http://" + ip + ":" + porta;
        String url = baseUrl + "/login.fcgi";

        RequisicaoLogin requisicaoLogin = new RequisicaoLogin(usuario, senha);
        String corpoJson = ProvedorMapeadorJson.get().writeValueAsString(requisicaoLogin);

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpoJson))
                .build();

        HttpResponse<String> resposta = httpClient.send(requisicao, HttpResponse.BodyHandlers.ofString());

        RespostaLogin respostaLogin = ProvedorMapeadorJson.get().readValue(resposta.body(), RespostaLogin.class);

        if (respostaLogin.hasError()) {                     // se a catraca devolveu um campo "error" preenchido
            throw new Exception("Erro no login: " + respostaLogin.getError());
        }

        if (!respostaLogin.hasSession()) {                  // se não veio erro, mas também não veio sessão (caso estranho)
            throw new Exception("Login sem token de sessão na resposta.");
        }

        this.sessionToken = respostaLogin.getSession();     // guarda o token no ATRIBUTO da classe, pra usar depois
        System.out.println("Login OK. Sessão: " + sessionToken);


        System.out.println("Status: " + resposta.statusCode());
        System.out.println("Corpo: " + resposta.body());
    }

    public void liberarCatraca(SentidoCatraca sentido) throws Exception { // MÉTODO: recebe o enum, não uma String solta
        if (sessionToken == null) { // usa o ATRIBUTO sessionToken que já existe na classe, preenchido pelo login()
            throw new Exception("Nenhuma sessão ativa. Faça login antes de liberar a catraca.");
        }

        ItemAcao item = new ItemAcao("catra", "allow=" + sentido.getValorApi());
        // ^ monta o item usando o valor exato da API que está guardado no enum

        RequisicaoExecutarAcoes requisicaoBody = new RequisicaoExecutarAcoes(List.of(item));
        // ^ List.of(item) cria uma lista pronta contendo só esse 1 item

        String corpoJson = ProvedorMapeadorJson.get().writeValueAsString(requisicaoBody);

        String url = baseUrl + "/execute_actions.fcgi?session=" + sessionToken;
        // ^ repare: aqui o token vai NA URL, como parâmetro "?session=...", não no corpo JSON

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpoJson))
                .build();

        HttpResponse<String> resposta = httpClient.send(requisicao, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status liberação (" + sentido.getRotulo() + "): " + resposta.statusCode());
        System.out.println("Corpo: " + resposta.body());
    }

}