package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Funcionario;
import com.castelodostorres.sistema.repositorio.FuncionarioRepositorio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import javafx.util.StringConverter;
import java.net.URL;
import java.sql.SQLException;
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
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboPapel.setItems(FXCollections.observableArrayList("GUIA", "RECEPCIONISTA"));
        comboPapel.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String valor) { // MÉTODO: converte o valor técnico pro texto exibido na tela
                if (valor == null) return "";
                return switch (valor) {
                    case "GUIA" -> "Guia";
                    case "RECEPCIONISTA" -> "Recepcionista";
                    default -> valor;
                };
            }

            @Override
            public String fromString(String texto) { // MÉTODO: caminho contrário, exigido pela interface, não usamos aqui
                return texto;
            }
        });

        comboTipoRemuneracao.setItems(FXCollections.observableArrayList("PERCENTUAL", "FIXO_POR_PESSOA"));
        comboTipoRemuneracao.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String valor) {
                if (valor == null) return "";
                return switch (valor) {
                    case "PERCENTUAL" -> "Percentual (%)";
                    case "FIXO_POR_PESSOA" -> "Valor Fixo por Pessoa (R$)";
                    default -> valor;
                };
            }

            @Override
            public String fromString(String texto) {
                return texto;
            }
        });
    }

    @FXML
    public void salvar() { // MÉTODO: chamado pelo clique do botão "Salvar"
        String nome = campoNome.getText();
        String papel = comboPapel.getValue();
        String tipoRemuneracao = comboTipoRemuneracao.getValue();
        String valorTexto = campoValor.getText();

        if (nome == null || nome.isBlank()) { // validação 1: nome não pode ficar vazio
            System.out.println("Informe o nome do funcionário.");
            return;
        }

        if (papel == null) { // validação 2: precisa ter selecionado uma função
            System.out.println("Selecione a função (Guia ou Recepcionista).");
            return;
        }

        if (tipoRemuneracao == null) { // validação 3: precisa ter selecionado o tipo de remuneração
            System.out.println("Selecione o tipo de remuneração.");
            return;
        }

        double valor;
        try {
            String valorNormalizado = valorTexto.replace(",", "."); // troca vírgula por ponto, se houver
            valor = Double.parseDouble(valorNormalizado);
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido, digite apenas números.");
            return;
        }

        Funcionario funcionario = new Funcionario(nome, papel, tipoRemuneracao, valor);

        FuncionarioRepositorio repositorio = new FuncionarioRepositorio();
        try {
            repositorio.salvar(funcionario);
            System.out.println("Funcionário salvo com sucesso! ID: " + funcionario.getId());
        } catch (SQLException e) {
            System.out.println("Erro ao salvar funcionário: " + e.getMessage());
        }
    }
}