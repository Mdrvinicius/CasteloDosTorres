package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Despesa;
import com.castelodostorres.sistema.modelo.FechamentoCaixa;
import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.modelo.dto.ComissaoFuncionario;
import com.castelodostorres.sistema.repositorio.DespesaRepositorio;
import com.castelodostorres.sistema.repositorio.FechamentoCaixaRepositorio;
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
    @FXML private TableView<FechamentoCaixa> tabelaFechamentos;
    @FXML private TableColumn<FechamentoCaixa, String> colFechData;
    @FXML private TableColumn<FechamentoCaixa, String> colFechFunc;
    @FXML private TableColumn<FechamentoCaixa, String> colFechDinEsp;
    @FXML private TableColumn<FechamentoCaixa, String> colFechDinCont;
    @FXML private TableColumn<FechamentoCaixa, String> colFechDinDiv;
    @FXML private TableColumn<FechamentoCaixa, String> colFechPixEsp;
    @FXML private TableColumn<FechamentoCaixa, String> colFechPixCont;
    @FXML private TableColumn<FechamentoCaixa, String> colFechPixDiv;
    @FXML private TableColumn<FechamentoCaixa, String> colFechStatus;
    @FXML private Label labelArrecadadoTopo;
    @FXML private Label labelTotalPagoTabela;
    @FXML private Label labelTotalDespesasTabela;
    @FXML private TableColumn<FechamentoCaixa, String> colFechFundoEsp;
    @FXML private TableColumn<FechamentoCaixa, String> colFechFundoReal;
    @FXML private TableColumn<FechamentoCaixa, String> colFechStatusFundo;
    @FXML private Label labelCustoLoja;    @FXML private TableColumn<ComissaoFuncionario, String> colunaPago;
    @FXML private TableColumn<ComissaoFuncionario, String> colunaFalta;

    private final DespesaRepositorio despesaRepositorio = new DespesaRepositorio();

    private final VisitaRepositorio repositorio = new VisitaRepositorio();
    private final CalculadoraComissao calculadoraComissao = new CalculadoraComissao();
    private final FechamentoCaixaRepositorio fechamentoRepositorio = new FechamentoCaixaRepositorio();
    private final com.castelodostorres.sistema.repositorio.VendaRepositorio vendaRepositorio =
            new com.castelodostorres.sistema.repositorio.VendaRepositorio();
    private final com.castelodostorres.sistema.repositorio.PagamentoFuncionarioRepositorio pagamentoRepositorio =
            new com.castelodostorres.sistema.repositorio.PagamentoFuncionarioRepositorio();

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

        colunaPago.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                "R$ " + String.format("%.2f", d.getValue().getPago())));
        colunaFalta.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                "R$ " + String.format("%.2f", d.getValue().getFaltaPagar())));

        colunaRazaoDespesa.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaValorDespesa.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colunaDataDespesa.setCellValueFactory(dados ->
                new javafx.beans.property.SimpleStringProperty(
                        FormatadorData.formatar(dados.getValue().getDataHoraCadastro())));
        colunaTipoDespesa.setCellValueFactory(dados ->
                new javafx.beans.property.SimpleStringProperty(
                        "RECORRENTE".equals(dados.getValue().getTipo()) ? "Recorrente" : "Avulsa"));

        colFechData.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                FormatadorData.formatar(d.getValue().getDataHoraFechamento())));
        colFechFunc.setCellValueFactory(new PropertyValueFactory<>("nomeFuncionario"));
        colFechDinEsp.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                "R$ " + String.format("%.2f", d.getValue().getDinheiroEsperado())));
        colFechDinCont.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                "R$ " + String.format("%.2f", d.getValue().getDinheiroContado())));
        colFechDinDiv.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                "R$ " + String.format("%.2f", d.getValue().getDivergenciaDinheiro())));
        colFechPixEsp.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                "R$ " + String.format("%.2f", d.getValue().getPixdebitoEsperado())));
        colFechPixCont.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                "R$ " + String.format("%.2f", d.getValue().getPixdebitoContado())));
        colFechPixDiv.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                "R$ " + String.format("%.2f", d.getValue().getDivergenciaPixdebito())));
        colFechStatus.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                statusFechamento(d.getValue())));
        colFechFundoEsp.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().isTemFundoHerdado() ? "R$ " + String.format("%.2f", d.getValue().getFundoHerdado()) : "—"));
        colFechFundoReal.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                "R$ " + String.format("%.2f", d.getValue().getFundoReal())));
        colFechStatusFundo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getStatusFundo()));

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
            // arrecadado = visitas + vendas da loja
            double arrecadado = repositorio.calcularTotalArrecadadoDoMes(mesTexto)
                    + vendaRepositorio.calcularArrecadadoDoMes(mesTexto);

            List<Visita> visitasDoMes = repositorio.listarDoMes(mesTexto);
            List<ComissaoFuncionario> comissoes = calculadoraComissao.calcular(visitasDoMes);
            java.util.Map<Integer, Double> pagosPorFuncionario = pagamentoRepositorio.somarPorFuncionarioNoMes(mesTexto);
            for (ComissaoFuncionario c : comissoes) {
                Double pago = pagosPorFuncionario.get(c.getFuncionarioId());
                c.setPago(pago == null ? 0.0 : pago);
            }
            tabelaComissao.setItems(FXCollections.observableArrayList(comissoes));

            double totalPago = 0;
            for (ComissaoFuncionario c : comissoes) {
                totalPago += c.getValor();
            }
            totalPago = Math.round(totalPago * 100.0) / 100.0; // limpa a dízima da soma

            // despesas do mês
            List<Despesa> despesas = despesaRepositorio.listarDoMes(mesTexto);

            double totalDespesas = 0;
            for (Despesa d : despesas) {
                totalDespesas += d.getValor();
            }
            tabelaDespesas.setItems(FXCollections.observableArrayList(despesas));

            List<FechamentoCaixa> fechamentos = fechamentoRepositorio.listarDoMes(mesTexto);
            tabelaFechamentos.setItems(FXCollections.observableArrayList(fechamentos));

            // custo dos produtos vendidos na loja no mês (desconta do líquido)
            double custoLoja = vendaRepositorio.calcularCustoLojaDoMes(mesTexto);

            double liquidoFinal = arrecadado - totalPago - totalDespesas - custoLoja;

            String arrecadadoTexto = "R$ " + String.format("%.2f", arrecadado);
            String pagoTexto = "R$ " + String.format("%.2f", totalPago);
            String despesasTexto = "R$ " + String.format("%.2f", totalDespesas);

            // cards da direita
            labelArrecadado.setText(arrecadadoTexto);
            labelTotalPago.setText(pagoTexto);
            labelTotalDespesas.setText(despesasTexto);
            labelCustoLoja.setText("R$ " + String.format("%.2f", custoLoja));
            labelLiquidoFinal.setText("R$ " + String.format("%.2f", liquidoFinal));

            // repetições na coluna esquerda
            labelArrecadadoTopo.setText(arrecadadoTexto);
            labelTotalPagoTabela.setText(pagoTexto);
            labelTotalDespesasTabela.setText(despesasTexto);
        } catch (SQLException e) {
            labelArrecadado.setText("Erro ao gerar resumo: " + e.getMessage());
        }
    }
    private String statusFechamento(FechamentoCaixa f) {
        boolean dinBate = Math.abs(f.getDivergenciaDinheiro()) < 0.001;
        boolean pixBate = Math.abs(f.getDivergenciaPixdebito()) < 0.001;
        if (dinBate && pixBate) return "Bateu";
        // se algum não bateu, indica faltou/sobrou pelo total
        double totalDiv = f.getDivergenciaDinheiro() + f.getDivergenciaPixdebito();
        return totalDiv < 0 ? "Faltou" : "Sobrou";
    }

    @FXML
    public void exportarPdf() { // MÉTODO: exporta o resumo do mês em PDF
        Integer mes = comboMes.getValue();
        if (mes == null) { labelArrecadado.setText("Selecione um mês."); return; }
        int ano;
        try { ano = Integer.parseInt(campoAno.getText().trim()); }
        catch (NumberFormatException e) { labelArrecadado.setText("Ano inválido."); return; }
        String mesTexto = String.format("%04d-%02d", ano, mes);

        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Salvar Resumo do Mês em PDF");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName("resumo-mes-" + mesTexto + "_gerado-" + java.time.LocalDate.now() + ".pdf");
        java.io.File destino = chooser.showSaveDialog(labelArrecadado.getScene().getWindow());
        if (destino == null) return;

        try {
            com.castelodostorres.sistema.util.GeradorPdf pdf = new com.castelodostorres.sistema.util.GeradorPdf();
            pdf.cabecalho("Resumo do Mês", "Período: " + String.format("%02d/%04d", mes, ano));

            double arrecadado = repositorio.calcularTotalArrecadadoDoMes(mesTexto)
                    + vendaRepositorio.calcularArrecadadoDoMes(mesTexto);

            List<Visita> visitasDoMes = repositorio.listarDoMes(mesTexto);
            List<ComissaoFuncionario> comissoes = calculadoraComissao.calcular(visitasDoMes);
            double totalPago = 0;
            for (ComissaoFuncionario c : comissoes) totalPago += c.getValor();
            totalPago = Math.round(totalPago * 100.0) / 100.0;

            List<Despesa> despesas = despesaRepositorio.listarDoMes(mesTexto);
            double totalDespesas = 0;
            for (Despesa d : despesas) totalDespesas += d.getValor();

            double custoLoja = vendaRepositorio.calcularCustoLojaDoMes(mesTexto);
            double liquidoFinal = arrecadado - totalPago - totalDespesas - custoLoja;

            pdf.secao("Resumo Financeiro");
            pdf.linha("Valor Total Arrecadado", "R$ " + String.format("%.2f", arrecadado));
            pdf.linha("Total Pago aos Funcionários", "R$ " + String.format("%.2f", totalPago));
            pdf.linha("Total de Despesas", "R$ " + String.format("%.2f", totalDespesas));
            pdf.linha("Custo de Loja", "R$ " + String.format("%.2f", custoLoja));
            pdf.linha("Valor Líquido Final", "R$ " + String.format("%.2f", liquidoFinal));

            pdf.secao("Pagamentos aos Funcionários");
            java.util.List<String[]> linhasComissao = new java.util.ArrayList<>();
            for (ComissaoFuncionario c : comissoes) {
                linhasComissao.add(new String[]{ c.getNome(), c.getPapel(), "R$ " + String.format("%.2f", c.getValor()) });
            }
            pdf.tabela(new String[]{ "Funcionário", "Função", "Total no Mês" },
                    linhasComissao, new float[]{ 200, 120, 120 });

            pdf.secao("Despesas");
            java.util.List<String[]> linhasDespesa = new java.util.ArrayList<>();
            for (Despesa d : despesas) {
                linhasDespesa.add(new String[]{
                        FormatadorData.formatar(d.getDataHoraCadastro()),
                        d.getNome(),
                        "RECORRENTE".equals(d.getTipo()) ? "Recorrente" : "Avulsa",
                        "R$ " + String.format("%.2f", d.getValor()) });
            }
            pdf.tabela(new String[]{ "Data", "Razão", "Tipo", "Valor" },
                    linhasDespesa, new float[]{ 110, 180, 100, 100 });

            pdf.secao("Fechamentos de Caixa");
            List<FechamentoCaixa> fechamentos = fechamentoRepositorio.listarDoMes(mesTexto);
            java.util.List<String[]> linhasFech = new java.util.ArrayList<>();
            for (FechamentoCaixa f : fechamentos) {
                linhasFech.add(new String[]{
                        FormatadorData.formatar(f.getDataHoraFechamento()),
                        f.getNomeFuncionario(),
                        "R$ " + String.format("%.2f", f.getDinheiroContado()),
                        "R$ " + String.format("%.2f", f.getPixdebitoContado()),
                        "R$ " + String.format("%.2f", f.getDivergenciaDinheiro()),
                        "R$ " + String.format("%.2f", f.getDivergenciaPixdebito()),
                        statusFechamento(f) });
            }
            pdf.tabela(new String[]{ "Data", "Fechou", "Dinheiro", "Pix+Déb", "Div.Din", "Div.Pix", "Status" },
                    linhasFech, new float[]{ 90, 90, 70, 70, 65, 65, 55 });

            pdf.salvarComo(destino);
            mostrarAvisoPdf("PDF salvo em:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            mostrarAvisoPdf("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    private void mostrarAvisoPdf(String m) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(m);
        a.showAndWait();
    }
}