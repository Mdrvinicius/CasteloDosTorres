package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.ItemVenda;
import com.castelodostorres.sistema.modelo.Produto;
import com.castelodostorres.sistema.modelo.Venda;
import com.castelodostorres.sistema.repositorio.ProdutoRepositorio;
import com.castelodostorres.sistema.repositorio.VendaRepositorio;
import com.castelodostorres.sistema.util.GerenciadorImagens;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorTelaLoja implements Initializable, PrecisaDaTelaRaiz {

    @FXML private TextField campoBusca;
    @FXML private ComboBox<String> comboCategoria;
    @FXML private FlowPane vitrine;
    @FXML private Label labelQtdCarrinho;
    @FXML private Label labelTotalCarrinho;
    @FXML private ComboBox<String> comboFormaPagamento;
    @FXML private VBox painelCarrinho;
    @FXML private VBox listaCarrinho;
    @FXML private Label labelTotalPainel;
    @FXML private Button botaoVerCarrinho;
    @FXML private VBox blocoDinheiro;
    @FXML private VBox blocoTroco;
    @FXML private TextField campoValorRecebido;
    @FXML private Label labelTroco;

    private final ProdutoRepositorio produtoRepositorio = new ProdutoRepositorio();
    private final VendaRepositorio vendaRepositorio = new VendaRepositorio();
    private ControladorTelaRaiz telaRaiz;

    private static class ItemCarrinho {
        Produto produto;
        int quantidade;
        ItemCarrinho(Produto p, int q) { this.produto = p; this.quantidade = q; }
        double subtotal() { return produto.getPrecoVenda() * quantidade; }
    }

    private final List<ItemCarrinho> carrinho = new ArrayList<>();

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) {
        this.telaRaiz = telaRaiz;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboFormaPagamento.setItems(FXCollections.observableArrayList("Dinheiro", "Pix", "Débito"));
        comboFormaPagamento.valueProperty().addListener((o, a, n) -> atualizarBlocoDinheiro());
        campoValorRecebido.textProperty().addListener((o, a, n) -> atualizarTroco());
        carregarCategorias();
        carregarVitrine(listarSeguro());
        atualizarRodape();
    }

    private void carregarCategorias() {
        try {
            List<String> cats = produtoRepositorio.listarCategorias();
            comboCategoria.getItems().clear();
            comboCategoria.getItems().add("Todas");
            comboCategoria.getItems().addAll(cats);
            comboCategoria.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            System.out.println("Erro ao carregar categorias: " + e.getMessage());
        }
    }

    private List<Produto> listarSeguro() {
        try {
            return produtoRepositorio.listarAtivos();
        } catch (SQLException e) {
            System.out.println("Erro ao carregar produtos: " + e.getMessage());
            return List.of();
        }
    }

    private void carregarVitrine(List<Produto> produtos) {
        vitrine.getChildren().clear();
        if (produtos.isEmpty()) {
            Label vazio = new Label("Nenhum produto cadastrado. Clique em 'Novo Produto' para começar.");
            vazio.getStyleClass().add("texto-suave");
            vitrine.getChildren().add(vazio);
            return;
        }
        for (Produto p : produtos) {
            vitrine.getChildren().add(criarCard(p));
        }
    }

    private VBox criarCard(Produto p) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card-produto");
        card.setPrefWidth(240);
        card.setAlignment(Pos.TOP_LEFT);

        VBox moldura = new VBox();
        moldura.getStyleClass().add("produto-imagem-moldura");
        moldura.setAlignment(Pos.CENTER);
        moldura.setPrefHeight(160);
        if (GerenciadorImagens.imagemExiste(p.getImagem())) {
            File f = GerenciadorImagens.caminhoImagem(p.getImagem());
            ImageView iv = new ImageView(new Image(f.toURI().toString()));
            iv.setFitWidth(200);
            iv.setFitHeight(150);
            iv.setPreserveRatio(true);
            moldura.getChildren().add(iv);
        } else {
            Label semFoto = new Label("Sem imagem");
            semFoto.getStyleClass().add("texto-suave");
            moldura.getChildren().add(semFoto);
        }

        Label nome = new Label(p.getNome());
        nome.getStyleClass().add("produto-nome");
        nome.setWrapText(true);

        Label preco = new Label("R$ " + String.format("%.2f", p.getPrecoVenda()));
        preco.getStyleClass().add("produto-preco");

        Label estoque = new Label("Estoque: " + p.getEstoque() + " un.");
        estoque.getStyleClass().add("produto-estoque");

        Button adicionar = new Button("Adicionar");
        adicionar.getStyleClass().add("botao-secundario");
        adicionar.setMaxWidth(Double.MAX_VALUE);
        adicionar.setOnAction(e -> adicionarAoCarrinho(p));

        card.getChildren().addAll(moldura, nome, preco, estoque, adicionar);
        return card;
    }

    private void adicionarAoCarrinho(Produto p) {
        ItemCarrinho existente = null;
        for (ItemCarrinho ic : carrinho) {
            if (ic.produto.getId().equals(p.getId())) { existente = ic; break; }
        }
        if (existente != null) {
            existente.quantidade++;
        } else {
            existente = new ItemCarrinho(p, 1);
            carrinho.add(existente);
        }
        if (existente.quantidade > p.getEstoque()) {
            mostrarAviso("Atenção: quantidade no carrinho (" + existente.quantidade +
                    ") maior que o estoque de \"" + p.getNome() + "\" (" + p.getEstoque() + " un.).");
        }
        atualizarRodape();
        if (painelCarrinho.isVisible()) montarLinhasCarrinho();
    }

    private void atualizarRodape() {
        int totalItens = 0;
        double total = 0;
        for (ItemCarrinho ic : carrinho) {
            totalItens += ic.quantidade;
            total += ic.subtotal();
        }
        labelQtdCarrinho.setText(totalItens + " item(ns)");
        labelTotalCarrinho.setText("R$ " + String.format("%.2f", total));
        atualizarTroco(); // total mudou, recalcula troco se estiver em dinheiro
    }

    private double totalCarrinho() {
        double total = 0;
        for (ItemCarrinho ic : carrinho) total += ic.subtotal();
        return total;
    }

    private void atualizarBlocoDinheiro() { // MÉTODO: mostra/esconde o bloco de dinheiro conforme a forma
        boolean ehDinheiro = "Dinheiro".equals(comboFormaPagamento.getValue());
        blocoDinheiro.setVisible(ehDinheiro);
        blocoDinheiro.setManaged(ehDinheiro);
        blocoTroco.setVisible(ehDinheiro);
        blocoTroco.setManaged(ehDinheiro);
        if (!ehDinheiro) {
            campoValorRecebido.clear();
            labelTroco.setText("R$ 0,00");
        } else {
            atualizarTroco();
        }
    }

    private void atualizarTroco() { // MÉTODO: calcula troco ao vivo (só display)
        if (!"Dinheiro".equals(comboFormaPagamento.getValue())) return;
        double recebido = lerValor(campoValorRecebido);
        double troco = recebido - totalCarrinho();
        if (troco < 0) troco = 0;
        labelTroco.setText("R$ " + String.format("%.2f", troco));
    }

    private double lerValor(TextField campo) {
        try {
            String t = campo.getText().replace(",", ".").trim();
            return t.isBlank() ? 0.0 : Double.parseDouble(t);
        } catch (NumberFormatException e) { return 0.0; }
    }

    @FXML
    public void alternarCarrinho() {
        boolean abrir = !painelCarrinho.isVisible();
        painelCarrinho.setVisible(abrir);
        painelCarrinho.setManaged(abrir);
        botaoVerCarrinho.setText(abrir ? "Ocultar Carrinho" : "Ver Carrinho");
        if (abrir) montarLinhasCarrinho();
    }

    private void montarLinhasCarrinho() {
        listaCarrinho.getChildren().clear();
        if (carrinho.isEmpty()) {
            Label vazio = new Label("O carrinho está vazio.");
            vazio.getStyleClass().add("texto-suave");
            listaCarrinho.getChildren().add(vazio);
            labelTotalPainel.setText("Total: R$ 0,00");
            return;
        }
        for (ItemCarrinho ic : carrinho) {
            HBox linha = new HBox(10);
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.getStyleClass().add("linha-carrinho");

            Label nome = new Label(ic.produto.getNome());
            nome.setPrefWidth(180);
            nome.getStyleClass().add("produto-nome");

            Label sub = new Label("R$ " + String.format("%.2f", ic.subtotal()));
            sub.setPrefWidth(100);
            sub.getStyleClass().add("produto-preco");

            Button menos = new Button("-");
            menos.getStyleClass().add("botao-contador");
            Label qtd = new Label(String.valueOf(ic.quantidade));
            qtd.setPrefWidth(34);
            qtd.setAlignment(Pos.CENTER);
            qtd.getStyleClass().add("valor-destaque");
            Button mais = new Button("+");
            mais.getStyleClass().add("botao-contador");
            Button remover = new Button("Remover");
            remover.getStyleClass().add("botao-perigo");

            menos.setOnAction(e -> {
                ic.quantidade--;
                if (ic.quantidade <= 0) carrinho.remove(ic);
                montarLinhasCarrinho();
                atualizarRodape();
            });
            mais.setOnAction(e -> {
                ic.quantidade++;
                if (ic.quantidade > ic.produto.getEstoque()) {
                    mostrarAviso("Quantidade maior que o estoque de \"" + ic.produto.getNome() + "\".");
                }
                montarLinhasCarrinho();
                atualizarRodape();
            });
            remover.setOnAction(e -> {
                carrinho.remove(ic);
                montarLinhasCarrinho();
                atualizarRodape();
            });

            Region esp = new Region();
            HBox.setHgrow(esp, Priority.ALWAYS);
            linha.getChildren().addAll(nome, sub, esp, menos, qtd, mais, remover);
            listaCarrinho.getChildren().add(linha);
        }
        labelTotalPainel.setText("Total: R$ " + String.format("%.2f", totalCarrinho()));
    }

    @FXML
    public void finalizarCompra() {
        if (carrinho.isEmpty()) { mostrarAviso("O carrinho está vazio."); return; }
        String forma = comboFormaPagamento.getValue();
        if (forma == null) { mostrarAviso("Selecione a forma de pagamento."); return; }

        double total = totalCarrinho();

        if ("Dinheiro".equals(forma)) {
            double recebido = lerValor(campoValorRecebido);
            if (recebido < total) {
                mostrarAviso("Valor recebido (R$ " + String.format("%.2f", recebido) +
                        ") é menor que o total (R$ " + String.format("%.2f", total) + ").");
                return;
            }
        }

        Venda venda = new Venda();
        venda.setDataHora(LocalDateTime.now().toString());
        venda.setValorTotal(total);
        venda.setValorDinheiro("Dinheiro".equals(forma) ? total : 0);
        venda.setValorPix("Pix".equals(forma) ? total : 0);
        venda.setValorDebito("Débito".equals(forma) ? total : 0);

        for (ItemCarrinho ic : carrinho) {
            ItemVenda iv = new ItemVenda();
            iv.setProdutoId(ic.produto.getId());
            iv.setNomeProduto(ic.produto.getNome());
            iv.setQuantidade(ic.quantidade);
            iv.setPrecoVendaUnitario(ic.produto.getPrecoVenda());
            iv.setPrecoCustoUnitario(ic.produto.getPrecoCusto());
            venda.adicionarItem(iv);
        }

        try {
            vendaRepositorio.salvar(venda);
            mostrarAviso("Venda registrada com sucesso! Total: R$ " + String.format("%.2f", total));
            carrinho.clear();
            comboFormaPagamento.getSelectionModel().clearSelection();
            campoValorRecebido.clear();
            atualizarBlocoDinheiro();
            atualizarRodape();
            painelCarrinho.setVisible(false);
            painelCarrinho.setManaged(false);
            botaoVerCarrinho.setText("Ver Carrinho");
            carregarVitrine(listarSeguro());
        } catch (SQLException e) {
            mostrarAviso("Erro ao registrar venda: " + e.getMessage());
        }
    }

    @FXML
    public void buscar() {
        String termo = campoBusca.getText();
        String cat = comboCategoria.getValue();
        if ("Todas".equals(cat)) cat = null;
        try {
            carregarVitrine(produtoRepositorio.buscarAtivos(termo, cat));
        } catch (SQLException e) {
            System.out.println("Erro na busca: " + e.getMessage());
        }
    }

    @FXML
    public void limparFiltros() {
        campoBusca.clear();
        comboCategoria.getSelectionModel().selectFirst();
        carregarVitrine(listarSeguro());
    }

    @FXML public void abrirRelatorioVendas() { if (telaRaiz != null) telaRaiz.abrirRelatorioVendas(); }
    @FXML public void abrirListaProdutos() { if (telaRaiz != null) telaRaiz.abrirListaProdutos(); }
    @FXML public void abrirCadastroProduto() { if (telaRaiz != null) telaRaiz.abrirCadastroProduto(); }

    private void mostrarAviso(String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(m);
        a.showAndWait();
    }
}