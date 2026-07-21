package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Despesa;
import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.modelo.dto.ComissaoFuncionario;
import com.castelodostorres.sistema.repositorio.DespesaRepositorio;
import com.castelodostorres.sistema.repositorio.VisitaRepositorio;
import com.castelodostorres.sistema.servico.CalculadoraComissao;
import com.castelodostorres.sistema.util.FormatadorData;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorTelaResumoMes implements Initializable {

    @FXML private ComboBox<Integer> comboMes;
    @FXML private TextField campoAno;
    @FXML private Label labelArrecadado;
    @FXML private Label labelTotalPago;
    @FXML private Label labelLiquidoFinal;
    @FXML private TableView<ComissaoFuncionario> tabelaComissao;
    @FXML private TableColumn<ComissaoFuncionario, String> colunaNome;
    @FXML private TableColumn<ComissaoFuncionario, String> colunaPapel;
    @FXML private TableColumn<ComissaoFuncionario, Double> colunaValor;
    @FXML private TableView<Despesa> tabelaDespesas;
    @FXML private TableColumn<Despesa, String> colunaDataDespesa;
    @FXML private TableColumn<Despesa, String> colunaRazaoDespesa;
    @FXML private TableColumn<Despesa, String> colunaTipoDespesa;
    @FXML private TableColumn<Despesa, Double> colunaValorDespesa;
    @FXML private Label labelTotalDespesas;

    private final DespesaRepositorio despesaRepositorio = new DespesaRepositorio();

    private final VisitaRepositorio repositorio = new VisitaRepositorio();
    private final CalculadoraComissao calculadoraComissao = new CalculadoraComissao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (int m = 1; m <= 12; m++) { // popula o combo com os meses 1 a 12
            comboMes.getItems().add(m);
        }
        LocalDate hoje = LocalDate.now();
        comboMes.setValue(hoje.getMonthValue()); // mês atual
        campoAno.setText(String.valueOf(hoje.getYear())); // ano atual

        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaPapel.setCellValueFactory(new PropertyValueFactory<>("papel"));
        colunaValor.setCellValueFactory(new PropertyValueFactory<>("valor"));

        colunaRazaoDespesa.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaValorDespesa.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colunaDataDespesa.setCellValueFactory(dados ->
                new javafx.beans.property.SimpleStringProperty(
                        FormatadorData.formatar(dados.getValue().getDataHoraCadastro())));
        colunaTipoDespesa.setCellValueFactory(dados ->
                new javafx.beans.property.SimpleStringProperty(
                        "RECORRENTE".equals(dados.getValue().getTipo()) ? "Recorrente" : "Avulsa"));

        gerar();
    }

    @FXML
    public void gerar() { // MÉTODO: calcula e mostra o resumo do mês selecionado
        Integer mes = comboMes.getValue();
        if (mes == null) {
            labelArrecadado.setText("Selecione um mês.");
            return;
        }

        int ano;
        try {
            ano = Integer.parseInt(campoAno.getText().trim());
        } catch (NumberFormatException e) {
            labelArrecadado.setText("Ano inválido.");
            return;
        }

        String mesTexto = String.format("%04d-%02d", ano, mes); // formato "aaaa-mm" (ex: "2026-07")

        try {
            double arrecadado = repositorio.calcularTotalArrecadadoDoMes(mesTexto);

            List<Visita> visitasDoMes = repositorio.listarDoMes(mesTexto);
            List<ComissaoFuncionario> comissoes = calculadoraComissao.calcular(visitasDoMes);
            tabelaComissao.setItems(FXCollections.observableArrayList(comissoes));

            double totalPago = 0;
            for (ComissaoFuncionario c : comissoes) {
                totalPago += c.getValor();
            }

            // despesas do mês

            List<Despesa> despesas = despesaRepositorio.listarDoMes(mesTexto);

            double totalDespesas = 0;
            for (Despesa d : despesas) {
                totalDespesas += d.getValor();
            }
            tabelaDespesas.setItems(FXCollections.observableArrayList(despesas));
            labelTotalDespesas.setText("Total de Despesas: R$ " + String.format("%.2f", totalDespesas));

            double liquidoFinal = arrecadado - totalPago - totalDespesas;

            labelArrecadado.setText("Valor Total Arrecadado: R$ " + String.format("%.2f", arrecadado));
            labelTotalPago.setText("Total Pago aos Funcionários: R$ " + String.format("%.2f", totalPago));
            labelLiquidoFinal.setText("Valor Líquido Final: R$ " + String.format("%.2f", liquidoFinal));
        } catch (SQLException e) {
            labelArrecadado.setText("Erro ao gerar resumo: " + e.getMessage());
        }
    }
}