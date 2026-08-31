package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Produto;
import com.castelodostorres.sistema.repositorio.ProdutoRepositorio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorTelaListaProdutos implements Initializable, PrecisaDaTelaRaiz {

    @FXML private TableView<Produto> tabelaProdutos;
    @FXML private TableColumn<Produto, String> colunaNome;
    @FXML private TableColumn<Produto, String> colunaCategoria;
    @FXML private TableColumn<Produto, String> colunaCusto;
    @FXML private TableColumn<Produto, String> colunaVenda;
    @FXML private TableColumn<Produto, Integer> colunaEstoque;
    @FXML private Label labelTotal;

    private final ProdutoRepositorio repositorio = new ProdutoRepositorio();
    private ControladorTelaRaiz telaRaiz;

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) {
        this.telaRaiz = telaRaiz;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaCategoria.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCategoria() == null ? "-" : d.getValue().getCategoria()));
        colunaCusto.setCellValueFactory(d ->
                new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getPrecoCusto())));
        colunaVenda.setCellValueFactory(d ->
                new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getPrecoVenda())));
        colunaEstoque.setCellValueFactory(new PropertyValueFactory<>("estoque"));

        configurarCliqueDuplo();
        carregar();
    }

    private void configurarCliqueDuplo() { // MÉTODO: duplo-clique abre a edição
        tabelaProdutos.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
                if (selecionado != null && telaRaiz != null) {
                    telaRaiz.abrirCadastroProdutoParaEditar(selecionado);
                }
            }
        });
    }

    private void carregar() { // MÉTODO: busca os produtos ativos e joga na tabela
        try {
            List<Produto> produtos = repositorio.listarAtivos();
            tabelaProdutos.setItems(FXCollections.observableArrayList(produtos));
            labelTotal.setText("Total de produtos: " + produtos.size());
        } catch (SQLException e) {
            System.out.println("Erro ao carregar produtos: " + e.getMessage());
        }
    }

    @FXML
    public void excluir() { // MÉTODO: soft delete com confirmação
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAviso("Selecione um produto para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Excluir Produto");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Tem certeza que deseja excluir \"" + selecionado.getNome() +
                "\"? Ele deixará de aparecer na loja, mas o histórico de vendas será preservado.");

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.OK) {
            return;
        }

        try {
            repositorio.desativar(selecionado.getId());
            mostrarAviso("Produto excluído com sucesso.");
            carregar();
        } catch (SQLException e) {
            mostrarAviso("Erro ao excluir: " + e.getMessage());
        }
    }

    @FXML
    public void novoProduto() {
        if (telaRaiz != null) telaRaiz.abrirCadastroProduto();
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
}