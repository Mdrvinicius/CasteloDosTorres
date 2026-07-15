package com.castelodostorres.sistema.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class ControladorTelaRaiz {

    @FXML private BorderPane painelRaiz; // ATRIBUTO: o BorderPane inteiro, pra podermos trocar o center dele

    @FXML
    public void abrirTelaPrincipal() { // MÉTODO: chamado pelo botão "Tela Principal"
        trocarConteudo("/com/castelodostorres/sistema/TelaPrincipal.fxml");
    }

    @FXML
    public void abrirFuncionarios() { // MÉTODO: por enquanto abre o cadastro atual
        trocarConteudo("/com/castelodostorres/sistema/CadastroFuncionario.fxml");
    }

    @FXML
    public void abrirConfiguracao() { // MÉTODO: será protegido por senha depois
        trocarConteudo("/com/castelodostorres/sistema/TelaConfiguracao.fxml");
    }

    @FXML
    public void abrirVisitas() {
        trocarConteudo("/com/castelodostorres/sistema/TelaVisitas.fxml");
    }

    @FXML
    public void abrirRelatorios() { // MÉTODO: tela ainda não existe
        mostrarEmConstrucao("Relatórios");
    }

    private void trocarConteudo(String caminhoFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Parent tela = loader.load();

            Object controlador = loader.getController(); // pega o Controller que o FXML acabou de criar
            if (controlador instanceof PrecisaDaTelaRaiz) { // se esse Controller implementa a interface...
                ((PrecisaDaTelaRaiz) controlador).setTelaRaiz(this); // ...entrega a referência da TelaRaiz (this) pra ele
            }

            painelRaiz.setCenter(tela);
        } catch (IOException e) {
            System.out.println("Erro ao abrir tela: " + e.getMessage());
        }
    }
    public void abrirDetalhesVisita(com.castelodostorres.sistema.modelo.Visita visita) { // MÉTODO: abre Detalhes passando a visita
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/castelodostorres/sistema/TelaDetalhesVisita.fxml"));
            Parent tela = loader.load();

            ControladorTelaDetalhesVisita controlador = loader.getController();
            controlador.setTelaRaiz(this);   // pra ela conseguir voltar
            controlador.setVisita(visita);    // entrega a visita a ser mostrada

            painelRaiz.setCenter(tela);
        } catch (IOException e) {
            System.out.println("Erro ao abrir detalhes: " + e.getMessage());
        }
    }

    private void mostrarEmConstrucao(String nome) { // MÉTODO: placeholder pras telas que faremos depois
        painelRaiz.setCenter(new Label("Tela de " + nome + " em construção."));
    }
}