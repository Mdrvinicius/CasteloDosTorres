package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Funcionario;
import com.castelodostorres.sistema.repositorio.FuncionarioRepositorio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorTelaPrincipal implements Initializable {

    @FXML private ComboBox<Funcionario> comboGuia;
    @FXML private ComboBox<Funcionario> comboRecepcionista;
    @FXML private Label labelInteira;
    @FXML private Label labelMeia;
    @FXML private Label labelNaoPagante;
    @FXML private TextArea campoObservacoes;
    @FXML private Label labelValorTotal;

    private int quantidadeInteira = 0; // ATRIBUTO: guarda o estado atual do contador
    private int quantidadeMeia = 0;
    private int quantidadeNaoPagante = 0;

    private final double valorInteira = 30.00; // ATRIBUTO: por enquanto fixo, depois vem da tabela configuracao
    private final double valorMeia = 15.00;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        carregarCombos();
    }

    private void carregarCombos() { // MÉTODO: busca no banco e popula os 2 combos
        FuncionarioRepositorio repositorio = new FuncionarioRepositorio();
        try {
            List<Funcionario> guias = repositorio.listarPorPapel("GUIA");
            comboGuia.setItems(FXCollections.observableArrayList(guias));
            comboGuia.setConverter(criarConversorFuncionario());

            List<Funcionario> recepcionistas = repositorio.listarPorPapel("RECEPCIONISTA");
            Funcionario nenhuma = new Funcionario(); // representa a opção "Nenhuma"
            nenhuma.setNome("Nenhuma");
            recepcionistas.add(0, nenhuma); // adiciona "Nenhuma" como primeiro item da lista

            comboRecepcionista.setItems(FXCollections.observableArrayList(recepcionistas));
            comboRecepcionista.setConverter(criarConversorFuncionario());
            comboRecepcionista.getSelectionModel().selectFirst(); // já inicia com "Nenhuma" selecionada
        } catch (SQLException e) {
            System.out.println("Erro ao carregar funcionários: " + e.getMessage());
        }
    }

    private StringConverter<Funcionario> criarConversorFuncionario() { // MÉTODO: reaproveitado pelos 2 combos
        return new StringConverter<>() {
            @Override
            public String toString(Funcionario funcionario) {
                return funcionario == null ? "" : funcionario.getNome();
            }

            @Override
            public Funcionario fromString(String texto) {
                return null;
            }
        };
    }

    @FXML public void incrementarInteira() { quantidadeInteira++; atualizarTela(); }
    @FXML public void decrementarInteira() { if (quantidadeInteira > 0) quantidadeInteira--; atualizarTela(); }
    @FXML public void incrementarMeia() { quantidadeMeia++; atualizarTela(); }
    @FXML public void decrementarMeia() { if (quantidadeMeia > 0) quantidadeMeia--; atualizarTela(); }
    @FXML public void incrementarNaoPagante() { quantidadeNaoPagante++; atualizarTela(); }
    @FXML public void decrementarNaoPagante() { if (quantidadeNaoPagante > 0) quantidadeNaoPagante--; atualizarTela(); }

    private void atualizarTela() { // MÉTODO: sincroniza os Labels da tela com o estado atual dos contadores
        labelInteira.setText(String.valueOf(quantidadeInteira));
        labelMeia.setText(String.valueOf(quantidadeMeia));
        labelNaoPagante.setText(String.valueOf(quantidadeNaoPagante));

        double total = (quantidadeInteira * valorInteira) + (quantidadeMeia * valorMeia);
        labelValorTotal.setText("Valor Total: R$ " + String.format("%.2f", total));
    }

    @FXML
    public void iniciarVisita() {
        System.out.println("Guia: " + comboGuia.getValue());
        System.out.println("Recepcionista: " + comboRecepcionista.getValue());
        System.out.println("Inteiras: " + quantidadeInteira + ", Meias: " + quantidadeMeia + ", Não pagantes: " + quantidadeNaoPagante);
        System.out.println("Observações: " + campoObservacoes.getText());
    }
}