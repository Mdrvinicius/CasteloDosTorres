package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Venda;
import com.castelodostorres.sistema.repositorio.VendaRepositorio;
import com.castelodostorres.sistema.util.FormatadorData;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorRelatorioVendasDia implements Initializable, PrecisaDaTelaRaiz {

    @FXML private DatePicker seletorData;
    @FXML private Label labelFaturamento;
    @FXML private Label labelCusto;
    @FXML private Label labelLucro;
    @FXML private TableView<Venda> tabelaVendas;
    @FXML private TableColumn<Venda, String> colData;
    @FXML private TableColumn<Venda, String> colTotal;
    @FXML private TableColumn<Venda, String> colDinheiro;
    @FXML private TableColumn<Venda, String> colPix;
    @FXML private TableColumn<Venda, String> colDebito;
    @FXML private TableColumn<Venda, String> colStatus;

    private final VendaRepositorio vendaRepositorio = new VendaRepositorio();
    private ControladorTelaRaiz telaRaiz;

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) {
        this.telaRaiz = telaRaiz;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colData.setCellValueFactory(d -> new SimpleStringProperty(FormatadorData.formatar(d.getValue().getDataHora())));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getValorTotal())));
        colDinheiro.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getValorDinheiro())));
        colPix.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getValorPix())));
        colDebito.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getValorDebito())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        configurarCliqueDuplo();
        seletorData.setValue(LocalDate.now());
        gerar();
    }

    private void configurarCliqueDuplo() {
        tabelaVendas.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                Venda sel = tabelaVendas.getSelectionModel().getSelectedItem();
                if (sel != null && telaRaiz != null) {
                    telaRaiz.abrirDetalhesVenda(sel);
                }
            }
        });
    }

    @FXML
    public void gerar() {
        LocalDate data = seletorData.getValue();
        if (data == null) { mostrarAviso("Selecione uma data."); return; }
        String dataTexto = data.toString();

        try {
            List<Venda> vendas = vendaRepositorio.listarDoDia(dataTexto);
            tabelaVendas.setItems(FXCollections.observableArrayList(vendas));

            double faturamento = vendaRepositorio.calcularArrecadadoDoDia(dataTexto);
            double custo = vendaRepositorio.calcularCustoLojaDoDia(dataTexto);
            double lucro = faturamento - custo;

            labelFaturamento.setText("R$ " + String.format("%.2f", faturamento));
            labelCusto.setText("R$ " + String.format("%.2f", custo));
            labelLucro.setText("R$ " + String.format("%.2f", lucro));
        } catch (SQLException e) {
            mostrarAviso("Erro ao gerar relatório: " + e.getMessage());
        }
    }

    @FXML
    public void cancelarVenda() {
        Venda sel = tabelaVendas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecione uma venda para cancelar."); return; }
        if ("CANCELADA".equals(sel.getStatus())) { mostrarAviso("Esta venda já está cancelada."); return; }

        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Cancelar Venda");
        dlg.setHeaderText("Informe o motivo do cancelamento:");
        dlg.setContentText("Motivo:");
        Optional<String> resp = dlg.showAndWait();
        if (resp.isEmpty()) return;
        String motivo = resp.get().trim();
        if (motivo.isBlank()) { mostrarAviso("O motivo é obrigatório."); return; }

        try {
            vendaRepositorio.cancelar(sel.getId(), motivo);
            mostrarAviso("Venda cancelada. O estoque dos produtos foi devolvido.");
            gerar();
        } catch (SQLException e) {
            mostrarAviso("Erro ao cancelar: " + e.getMessage());
        }
    }

    @FXML
    public void abrirMes() {
        if (telaRaiz != null) telaRaiz.abrirRelatorioVendasMes();
    }

    @FXML
    public void voltar() {
        if (telaRaiz != null) telaRaiz.abrirLoja();
    }

    private void mostrarAviso(String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(m);
        a.showAndWait();
    }

    @FXML
    public void exportarPdf() { // MÉTODO: exporta vendas da loja do dia (com itens por venda)
        LocalDate data = seletorData.getValue();
        if (data == null) { mostrarAviso("Selecione uma data."); return; }
        String dataTexto = data.toString();

        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Salvar Vendas do Dia em PDF");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName("vendas-dia-" + dataTexto + ".pdf");
        java.io.File destino = chooser.showSaveDialog(labelFaturamento.getScene().getWindow());
        if (destino == null) return;

        try {
            com.castelodostorres.sistema.util.GeradorPdf pdf = new com.castelodostorres.sistema.util.GeradorPdf();
            pdf.cabecalho("Vendas da Loja - Dia",
                    "Data: " + FormatadorData.formatar(data.atStartOfDay().toString()));

            double faturamento = vendaRepositorio.calcularArrecadadoDoDia(dataTexto);
            double custo = vendaRepositorio.calcularCustoLojaDoDia(dataTexto);
            double lucro = faturamento - custo;

            pdf.secao("Resumo");
            pdf.linha("Faturamento", "R$ " + String.format("%.2f", faturamento));
            pdf.linha("Custo", "R$ " + String.format("%.2f", custo));
            pdf.linha("Lucro", "R$ " + String.format("%.2f", lucro));

            pdf.secao("Vendas");
            List<Venda> vendas = vendaRepositorio.listarDoDia(dataTexto);
            for (Venda v : vendas) {
                String forma = v.getValorDinheiro() > 0 ? "Dinheiro" : (v.getValorPix() > 0 ? "Pix" : (v.getValorDebito() > 0 ? "Débito" : "-"));
                pdf.linha(FormatadorData.formatar(v.getDataHora()) + " — " + v.getStatus(),
                        "R$ " + String.format("%.2f", v.getValorTotal()) + " (" + forma + ")");
                java.util.List<com.castelodostorres.sistema.modelo.ItemVenda> itens = vendaRepositorio.listarItens(v.getId());
                java.util.List<String[]> linhasItem = new java.util.ArrayList<>();
                for (com.castelodostorres.sistema.modelo.ItemVenda iv : itens) {
                    linhasItem.add(new String[]{
                            iv.getQuantidade() + "x " + iv.getNomeProduto(),
                            "R$ " + String.format("%.2f", iv.getPrecoVendaUnitario()),
                            "R$ " + String.format("%.2f", iv.getSubtotalVenda()) });
                }
                pdf.tabela(new String[]{ "Produto", "Preço Unit.", "Subtotal" },
                        linhasItem, new float[]{ 240, 110, 110 });
                pdf.espaco(8);
            }

            pdf.salvarComo(destino);
            mostrarAviso("PDF salvo em:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            mostrarAviso("Erro ao gerar PDF: " + e.getMessage());
        }
    }
}