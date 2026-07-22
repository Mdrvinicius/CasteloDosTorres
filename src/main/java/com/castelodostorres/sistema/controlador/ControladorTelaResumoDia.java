package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.modelo.dto.ComissaoFuncionario;
import com.castelodostorres.sistema.repositorio.VisitaRepositorio;
import com.castelodostorres.sistema.servico.CalculadoraComissao;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorTelaResumoDia implements Initializable {

    @FXML private DatePicker seletorData;
    @FXML private Label labelTotal;
    @FXML private Label labelDinheiro;
    @FXML private Label labelPix;
    @FXML private Label labelDebito;
    @FXML private TableView<ComissaoFuncionario> tabelaComissao;
    @FXML private TableColumn<ComissaoFuncionario, String> colunaNome;
    @FXML private TableColumn<ComissaoFuncionario, String> colunaPapel;
    @FXML private TableColumn<ComissaoFuncionario, Double> colunaValor;
    @FXML private Label labelTotalComissao;
    @FXML private Label labelQtdVisitas;
    @FXML private Label labelTotalInteiras;
    @FXML private Label labelTotalMeias;
    @FXML private Label labelTotalNaoPagantes;
    @FXML private Label labelTotalReembolsos;
    @FXML private Label labelValorFinal;
    @FXML private Label labelAgendadosDia;

    private final VisitaRepositorio repositorio = new VisitaRepositorio();
    private final CalculadoraComissao calculadoraComissao = new CalculadoraComissao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaPapel.setCellValueFactory(new PropertyValueFactory<>("papel"));
        colunaValor.setCellValueFactory(new PropertyValueFactory<>("valor"));

        seletorData.setValue(LocalDate.now());
        gerar();
    }

    @FXML
    public void gerar() { // MÉTODO: calcula e mostra o total do dia selecionado
        LocalDate data = seletorData.getValue();
        if (data == null) {
            labelTotal.setText("Selecione uma data.");
            return;
        }

        String dataTexto = data.toString(); // LocalDate.toString() já dá "aaaa-mm-dd"

        try {
            double total = repositorio.calcularTotalArrecadadoDoDia(dataTexto);
            labelTotal.setText("R$ " + String.format("%.2f", total));

            double[] formas = repositorio.calcularFormasPagamentoDoDia(dataTexto);
            labelDinheiro.setText("R$ " + String.format("%.2f", formas[0]));
            labelPix.setText("R$ " + String.format("%.2f", formas[1]));
            labelDebito.setText("R$ " + String.format("%.2f", formas[2]));

            List<Visita> visitasDoDia = repositorio.listarDoDia(dataTexto);
            List<ComissaoFuncionario> comissoes = calculadoraComissao.calcular(visitasDoDia);
            tabelaComissao.setItems(FXCollections.observableArrayList(comissoes));

            double[] agendados = repositorio.calcularAgendadosDoDia(dataTexto);
            labelAgendadosDia.setText("Valor de agendados: dinheiro R$ " + String.format("%.2f", agendados[0]) +
                    " | pix R$ " + String.format("%.2f", agendados[1]) +
                    " | débito R$ " + String.format("%.2f", agendados[2]));

            double totalComissao = 0;
            for (ComissaoFuncionario c : comissoes) {
                totalComissao += c.getValor();
            }
            labelTotalComissao.setText("R$ " + String.format("%.2f", totalComissao));

            double[] est = repositorio.calcularEstatisticasDoDia(dataTexto);
            labelQtdVisitas.setText(String.valueOf((int) est[0]));
            labelTotalInteiras.setText(String.valueOf((int) est[1]));
            labelTotalMeias.setText(String.valueOf((int) est[2]));
            labelTotalNaoPagantes.setText(String.valueOf((int) est[3]));

            double totalReembolsos = est[5];
            labelTotalReembolsos.setText("R$ " + String.format("%.2f", totalReembolsos));

            // valor final = arrecadado líquido - comissão
            // (o 'total' já é líquido: valor_total - valor_reembolsado; então não subtrai reembolso de novo)
            double valorFinal = total - totalComissao;
            labelValorFinal.setText("R$ " + String.format("%.2f", valorFinal));

        } catch (SQLException e) {
            labelTotal.setText("Erro ao gerar resumo: " + e.getMessage());
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