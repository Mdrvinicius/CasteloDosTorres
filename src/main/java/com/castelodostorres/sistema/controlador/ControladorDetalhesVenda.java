package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.ItemVenda;
import com.castelodostorres.sistema.modelo.Venda;
import com.castelodostorres.sistema.repositorio.VendaRepositorio;
import com.castelodostorres.sistema.util.FormatadorData;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;

public class ControladorDetalhesVenda implements PrecisaDaTelaRaiz {

    @FXML private Label labelData;
    @FXML private Label labelTotal;
    @FXML private Label labelForma;
    @FXML private Label labelStatus;
    @FXML private Label labelMotivo;
    @FXML private TableView<ItemVenda> tabelaItens;
    @FXML private TableColumn<ItemVenda, String> colProduto;
    @FXML private TableColumn<ItemVenda, Integer> colQtd;
    @FXML private TableColumn<ItemVenda, String> colPrecoUnit;
    @FXML private TableColumn<ItemVenda, String> colSubtotal;

    private final VendaRepositorio vendaRepositorio = new VendaRepositorio();
    private ControladorTelaRaiz telaRaiz;
    private Venda venda;

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) {
        this.telaRaiz = telaRaiz;
    }

    public void setVenda(Venda venda) { // MÉTODO: recebe a venda e preenche
        this.venda = venda;
        preencher();
    }

    @FXML
    public void initialize() {
        colProduto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colQtd.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantidade"));
        colPrecoUnit.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getPrecoVendaUnitario())));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getSubtotalVenda())));
    }

    private void preencher() {
        labelData.setText(FormatadorData.formatar(venda.getDataHora()));
        labelTotal.setText("R$ " + String.format("%.2f", venda.getValorTotal()));
        labelForma.setText(formaPagamento());
        labelStatus.setText(venda.getStatus());
        labelStatus.getStyleClass().removeAll("badge-ativa", "badge-cancelada");
        labelStatus.getStyleClass().add("CANCELADA".equals(venda.getStatus()) ? "badge-cancelada" : "badge-ativa");

        if ("CANCELADA".equals(venda.getStatus()) && venda.getMotivoCancelamento() != null) {
            labelMotivo.setText("Motivo do cancelamento: " + venda.getMotivoCancelamento());
            labelMotivo.setVisible(true);
            labelMotivo.setManaged(true);
        } else {
            labelMotivo.setVisible(false);
            labelMotivo.setManaged(false);
        }

        try {
            List<ItemVenda> itens = vendaRepositorio.listarItens(venda.getId());
            tabelaItens.setItems(FXCollections.observableArrayList(itens));
        } catch (SQLException e) {
            System.out.println("Erro ao carregar itens: " + e.getMessage());
        }
    }

    private String formaPagamento() { // MÉTODO: descobre a forma pela qual foi pago
        if (venda.getValorDinheiro() > 0) return "Dinheiro";
        if (venda.getValorPix() > 0) return "Pix";
        if (venda.getValorDebito() > 0) return "Débito";
        return "-";
    }

    @FXML
    public void voltar() {
        if (telaRaiz != null) telaRaiz.abrirRelatorioVendas();
    }
}