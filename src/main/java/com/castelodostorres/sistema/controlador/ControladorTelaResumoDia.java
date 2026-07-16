package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.modelo.dto.ComissaoFuncionario;
import com.castelodostorres.sistema.repositorio.VisitaRepositorio;
import com.castelodostorres.sistema.servico.CalculadoraComissao;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
            labelTotal.setText("Total Arrecadado: R$ " + String.format("%.2f", total));

            double[] formas = repositorio.calcularFormasPagamentoDoDia(dataTexto);
            labelDinheiro.setText("Dinheiro: R$ " + String.format("%.2f", formas[0]));
            labelPix.setText("Pix: R$ " + String.format("%.2f", formas[1]));
            labelDebito.setText("Débito: R$ " + String.format("%.2f", formas[2]));

            List<Visita> visitasDoDia = repositorio.listarDoDia(dataTexto);
            List<ComissaoFuncionario> comissoes = calculadoraComissao.calcular(visitasDoDia);
            tabelaComissao.setItems(FXCollections.observableArrayList(comissoes));

            double totalComissao = 0;
            for (ComissaoFuncionario c : comissoes) {
                totalComissao += c.getValor();

                double[] est = repositorio.calcularEstatisticasDoDia(dataTexto);
                labelQtdVisitas.setText("Visitas: " + (int) est[0]);
                labelTotalInteiras.setText("Inteiras: " + (int) est[1]);
                labelTotalMeias.setText("Meias: " + (int) est[2]);
                labelTotalNaoPagantes.setText("Não Pagantes: " + (int) est[3]);

                double totalReembolsos = est[5];
                labelTotalReembolsos.setText("Total de Reembolsos: R$ " + String.format("%.2f", totalReembolsos));

                // valor final = arrecadado líquido - comissão
                // (o 'total' já é líquido: valor_total - valor_reembolsado; então não subtrai reembolso de novo)
                double valorFinal = total - totalComissao;
                labelValorFinal.setText("Valor Final do Dia: R$ " + String.format("%.2f", valorFinal));
            }
            labelTotalComissao.setText("Total a Pagar: R$ " + String.format("%.2f", totalComissao));
        } catch (SQLException e) {
            labelTotal.setText("Erro ao gerar resumo: " + e.getMessage());
        }


    }


}