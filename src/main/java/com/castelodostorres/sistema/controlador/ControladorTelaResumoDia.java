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
    private final com.castelodostorres.sistema.repositorio.VendaRepositorio vendaRepositorio =
            new com.castelodostorres.sistema.repositorio.VendaRepositorio();

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
            double totalVisitas = repositorio.calcularTotalArrecadadoDoDia(dataTexto);
            double totalVendas = vendaRepositorio.calcularArrecadadoDoDia(dataTexto);
            double total = totalVisitas + totalVendas;
            labelTotal.setText("R$ " + String.format("%.2f", total));

            // formas de pagamento = visitas (não-agendadas) + vendas da loja
            double[] formasVisita = repositorio.calcularFormasPagamentoDoDia(dataTexto);
            double[] formasVenda = vendaRepositorio.calcularFormasPagamentoDoDia(dataTexto);
            labelDinheiro.setText("R$ " + String.format("%.2f", formasVisita[0] + formasVenda[0]));
            labelPix.setText("R$ " + String.format("%.2f", formasVisita[1] + formasVenda[1]));
            labelDebito.setText("R$ " + String.format("%.2f", formasVisita[2] + formasVenda[2]));

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
            totalComissao = Math.round(totalComissao * 100.0) / 100.0; // limpa a dízima da soma
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

    @FXML
    public void exportarPdf() { // MÉTODO: exporta o resumo do dia em PDF
        LocalDate data = seletorData.getValue();
        if (data == null) { mostrarAviso("Selecione uma data."); return; }
        String dataTexto = data.toString();

        // escolher onde salvar
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Salvar Resumo do Dia em PDF");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName("resumo-dia-" + dataTexto + "_gerado-" + java.time.LocalDate.now() + ".pdf");
        java.io.File destino = chooser.showSaveDialog(labelTotal.getScene().getWindow());
        if (destino == null) return; // cancelou

        try {
            com.castelodostorres.sistema.util.GeradorPdf pdf = new com.castelodostorres.sistema.util.GeradorPdf();
            pdf.cabecalho("Resumo do Dia",
                    "Data: " + com.castelodostorres.sistema.util.FormatadorData.formatar(data.atStartOfDay().toString()));

            // recalcula os valores (mesmos do gerar)
            double totalVisitas = repositorio.calcularTotalArrecadadoDoDia(dataTexto);
            double totalVendas = vendaRepositorio.calcularArrecadadoDoDia(dataTexto);
            double total = totalVisitas + totalVendas;

            double[] formasVisita = repositorio.calcularFormasPagamentoDoDia(dataTexto);
            double[] formasVenda = vendaRepositorio.calcularFormasPagamentoDoDia(dataTexto);

            double[] est = repositorio.calcularEstatisticasDoDia(dataTexto);

            java.util.List<com.castelodostorres.sistema.modelo.Visita> visitasDoDia = repositorio.listarDoDia(dataTexto);
            java.util.List<com.castelodostorres.sistema.modelo.dto.ComissaoFuncionario> comissoes =
                    calculadoraComissao.calcular(visitasDoDia);
            double totalComissao = 0;
            for (com.castelodostorres.sistema.modelo.dto.ComissaoFuncionario c : comissoes) totalComissao += c.getValor();
            totalComissao = Math.round(totalComissao * 100.0) / 100.0;

            double reembolsos = est[5];
            double valorFinal = total - totalComissao;

            pdf.secao("Resumo Geral");
            pdf.linha("Visitas", String.valueOf((int) est[0]));
            pdf.linha("Inteiras", String.valueOf((int) est[1]));
            pdf.linha("Meias", String.valueOf((int) est[2]));
            pdf.linha("Não Pagantes", String.valueOf((int) est[3]));
            pdf.linha("Total Arrecadado", "R$ " + String.format("%.2f", total));

            pdf.secao("Formas de Pagamento");
            pdf.linha("Dinheiro", "R$ " + String.format("%.2f", formasVisita[0] + formasVenda[0]));
            pdf.linha("Pix", "R$ " + String.format("%.2f", formasVisita[1] + formasVenda[1]));
            pdf.linha("Débito", "R$ " + String.format("%.2f", formasVisita[2] + formasVenda[2]));

            pdf.secao("Pagamento aos Funcionários");
            java.util.List<String[]> linhasComissao = new java.util.ArrayList<>();
            for (com.castelodostorres.sistema.modelo.dto.ComissaoFuncionario c : comissoes) {
                linhasComissao.add(new String[]{ c.getNome(), c.getPapel(), "R$ " + String.format("%.2f", c.getValor()) });
            }
            pdf.tabela(new String[]{ "Funcionário", "Função", "A Receber" },
                    linhasComissao,
                    new float[]{ 200, 120, 120 });
            pdf.espaco(4);
            pdf.linha("Total a Pagar", "R$ " + String.format("%.2f", totalComissao));

            pdf.secao("Fechamento");
            pdf.linha("Total de Reembolsos", "R$ " + String.format("%.2f", reembolsos));
            pdf.linha("Valor Final do Dia", "R$ " + String.format("%.2f", valorFinal));

            pdf.salvarComo(destino);
            mostrarAviso("PDF salvo com sucesso em:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            mostrarAviso("Erro ao gerar PDF: " + e.getMessage());
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