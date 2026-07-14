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
    public void abrirVisitas() { // MÉTODO: tela ainda não existe
        mostrarEmConstrucao("Visitas");
    }

    @FXML
    public void abrirRelatorios() { // MÉTODO: tela ainda não existe
        mostrarEmConstrucao("Relatórios");
    }

    private void trocarConteudo(String caminhoFxml) { // MÉTODO: carrega um FXML e coloca no centro do painel
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Parent tela = loader.load();
            painelRaiz.setCenter(tela);
        } catch (IOException e) {
            System.out.println("Erro ao abrir tela: " + e.getMessage());
        }
    }

    private void mostrarEmConstrucao(String nome) { // MÉTODO: placeholder pras telas que faremos depois
        painelRaiz.setCenter(new Label("Tela de " + nome + " em construção."));
    }
}