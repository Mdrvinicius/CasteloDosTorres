package com.castelodostorres.sistema.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ControladorCadastroFuncionario implements Initializable {

    @FXML
    private TextField campoNome; // ATRIBUTO: representa o TextField do FXML com fx:id="campoNome"

    @FXML
    private ComboBox<String> comboPapel; // ATRIBUTO: representa o ComboBox fx:id="comboPapel"

    @FXML
    private ComboBox<String> comboTipoRemuneracao; // ATRIBUTO: representa o ComboBox fx:id="comboTipoRemuneracao"

    @FXML
    private TextField campoValor; // ATRIBUTO: representa o TextField fx:id="campoValor"

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // MÉTODO: chamado automaticamente ao carregar a tela
        comboPapel.setItems(FXCollections.observableArrayList("GUIA", "RECEPCIONISTA"));
        comboTipoRemuneracao.setItems(FXCollections.observableArrayList("PERCENTUAL", "FIXO_POR_PESSOA"));
    }

    @FXML
    public void salvar() { // MÉTODO: chamado automaticamente pelo onAction="#salvar" do botão
        String nome = campoNome.getText();
        String papel = comboPapel.getValue();
        String tipoRemuneracao = comboTipoRemuneracao.getValue();
        String valorTexto = campoValor.getText();

        System.out.println("Nome: " + nome);
        System.out.println("Papel: " + papel);
        System.out.println("Tipo remuneração: " + tipoRemuneracao);
        System.out.println("Valor: " + valorTexto);
    }
}