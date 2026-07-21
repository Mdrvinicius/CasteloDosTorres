package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Despesa;
import com.castelodostorres.sistema.repositorio.DespesaRepositorio;
import com.castelodostorres.sistema.util.FormatadorData;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorTelaDespesas implements Initializable {

    @FXML private Label labelFormulario;
    @FXML private TextField campoNome;
    @FXML private TextField campoValor;
    @FXML private ComboBox<String> comboTipo;
    @FXML private Button botaoCancelarEdicao;
    @FXML private TableView<Despesa> tabelaDespesas;
    @FXML private TableColumn<Despesa, String> colunaNome;
    @FXML private TableColumn<Despesa, String> colunaTipo;
    @FXML private TableColumn<Despesa, Double> colunaValor;
    @FXML private TableColumn<Despesa, String> colunaData;

    private final DespesaRepositorio repositorio = new DespesaRepositorio();
    private Despesa despesaEmEdicao; // null = cadastrando nova; preenchido = editando

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboTipo.setItems(FXCollections.observableArrayList("AVULSA", "RECORRENTE"));
        comboTipo.setConverter(new StringConverter<>() {
            @Override public String toString(String v) {
                if (v == null) return "";
                return v.equals("RECORRENTE") ? "Recorrente" : "Avulsa";
            }
            @Override public String fromString(String s) { return s; }
        });

        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colunaTipo.setCellValueFactory(dados ->
                new javafx.beans.property.SimpleStringProperty(
                        "RECORRENTE".equals(dados.getValue().getTipo()) ? "Recorrente" : "Avulsa"));
        colunaData.setCellValueFactory(dados ->
                new javafx.beans.property.SimpleStringProperty(
                        FormatadorData.formatar(dados.getValue().getDataHoraCadastro())));

        configurarDuploClique();
        cancelarEdicao(); // começa em modo "nova despesa"
        carregarDespesas();
    }

    private void configurarDuploClique() {
        tabelaDespesas.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                Despesa selecionada = tabelaDespesas.getSelectionModel().getSelectedItem();
                if (selecionada != null) entrarEmEdicao(selecionada);
            }
        });
    }

    private void entrarEmEdicao(Despesa despesa) { // MÉTODO: preenche o formulário com a despesa pra editar
        despesaEmEdicao = despesa;
        labelFormulario.setText("Editar Despesa");
        campoNome.setText(despesa.getNome());
        campoValor.setText(String.format("%.2f", despesa.getValor()));
        comboTipo.setValue(despesa.getTipo());
        botaoCancelarEdicao.setVisible(true);
        botaoCancelarEdicao.setManaged(true);
    }

    @FXML
    public void cancelarEdicao() { // MÉTODO: volta pro modo "nova despesa"
        despesaEmEdicao = null;
        labelFormulario.setText("Nova Despesa");
        campoNome.clear();
        campoValor.clear();
        comboTipo.getSelectionModel().clearSelection();
        botaoCancelarEdicao.setVisible(false);
        botaoCancelarEdicao.setManaged(false);
    }

    @FXML
    public void salvar() { // MÉTODO: cadastra nova OU atualiza a em edição
        String nome = campoNome.getText();
        String tipo = comboTipo.getValue();

        if (nome == null || nome.isBlank()) { mostrarAviso("Informe o nome/razão."); return; }
        if (tipo == null) { mostrarAviso("Selecione o tipo."); return; }

        double valor;
        try {
            valor = Double.parseDouble(campoValor.getText().replace(",", ".").trim());
        } catch (NumberFormatException e) { mostrarAviso("Valor inválido."); return; }
        if (valor <= 0) { mostrarAviso("O valor deve ser maior que zero."); return; }

        try {
            if (despesaEmEdicao == null) { // cadastro novo
                Despesa nova = new Despesa();
                nova.setNome(nome);
                nova.setValor(valor);
                nova.setTipo(tipo);
                nova.setDataHoraCadastro(LocalDateTime.now().toString());
                repositorio.salvar(nova);
                mostrarAviso("Despesa cadastrada com sucesso.");
            } else { // edição
                despesaEmEdicao.setNome(nome);
                despesaEmEdicao.setValor(valor);
                despesaEmEdicao.setTipo(tipo);
                repositorio.atualizar(despesaEmEdicao);
                mostrarAviso("Despesa atualizada com sucesso.");
            }
            cancelarEdicao();
            carregarDespesas();
        } catch (SQLException e) {
            mostrarAviso("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    public void apagar() { // MÉTODO: apaga a despesa selecionada
        Despesa selecionada = tabelaDespesas.getSelectionModel().getSelectedItem();
        if (selecionada == null) { mostrarAviso("Selecione uma despesa para apagar."); return; }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Apagar Despesa");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Apagar a despesa \"" + selecionada.getNome() + "\"?");
        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.OK) return;

        try {
            repositorio.apagar(selecionada.getId());
            mostrarAviso("Despesa apagada.");
            carregarDespesas();
        } catch (SQLException e) {
            mostrarAviso("Erro ao apagar: " + e.getMessage());
        }
    }

    private void carregarDespesas() {
        try {
            List<Despesa> despesas = repositorio.listarTodas();
            tabelaDespesas.setItems(FXCollections.observableArrayList(despesas));
        } catch (SQLException e) {
            mostrarAviso("Erro ao carregar despesas: " + e.getMessage());
        }
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Aviso");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}