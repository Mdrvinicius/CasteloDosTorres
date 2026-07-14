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
    private String baseUrl;// ATRIBUTO NOVO: guarda "http://IP:porta" após o login, pra reusar depois
    private String usuario;
    private String senha;


    public ServicoControlId() {
        this.httpClient = HttpClient.newHttpClient();
    }
    public void configurar(String ip, int porta, String usuario, String senha) { // MÉTODO: recebe os dados de conexão do Controller
        this.baseUrl = "http://" + ip + ":" + porta;
        this.usuario = usuario;
        this.senha = senha;
    }

    public void login() throws Exception { // MÉTODO: agora sem parâmetros, usa os dados já guardados por configurar()
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

        if (respostaLogin.hasError()) {
            throw new Exception("Erro no login: " + respostaLogin.getError());
        }
        if (!respostaLogin.hasSession()) {
            throw new Exception("Login sem token de sessão na resposta.");
        }

        this.sessionToken = respostaLogin.getSession();
    }

    public void liberarCatraca(SentidoCatraca sentido) throws Exception { // MÉTODO público: com retry automático
        if (sessionToken == null) { // se nunca logou ainda, loga primeiro
            login();
        }

        int statusCode = enviarLiberacao(sentido); // primeira tentativa

        if (statusCode == 401) { // 401 = não autorizado = token expirou
            login();                        // reloga
            statusCode = enviarLiberacao(sentido); // tenta de novo
        }

        if (statusCode != 200) { // se mesmo após relogar não deu 200, é erro real
            throw new Exception("Falha ao liberar catraca. Código: " + statusCode);
        }
    }

    private int enviarLiberacao(SentidoCatraca sentido) throws Exception { // MÉTODO privado: só envia o comando e devolve o status
        ItemAcao item = new ItemAcao("catra", "allow=" + sentido.getValorApi());
        RequisicaoExecutarAcoes requisicaoBody = new RequisicaoExecutarAcoes(List.of(item));
        String corpoJson = ProvedorMapeadorJson.get().writeValueAsString(requisicaoBody);

        String url = baseUrl + "/execute_actions.fcgi?session=" + sessionToken;

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpoJson))
                .build();

        HttpResponse<String> resposta = httpClient.send(requisicao, HttpResponse.BodyHandlers.ofString());
        return resposta.statusCode();
    }

}