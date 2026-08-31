package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Produto;
import com.castelodostorres.sistema.repositorio.ProdutoRepositorio;
import com.castelodostorres.sistema.util.GerenciadorImagens;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;

public class ControladorCadastroProduto implements PrecisaDaTelaRaiz {

    @FXML private Label labelTitulo;
    @FXML private TextField campoNome;
    @FXML private TextField campoCategoria;
    @FXML private TextField campoCusto;
    @FXML private TextField campoVenda;
    @FXML private TextField campoEstoque;
    @FXML private ImageView previewImagem;
    @FXML private Label labelSemImagem;
    @FXML private Button botaoRemoverImagem;
    @FXML private Button botaoCancelarEdicao;

    private final ProdutoRepositorio repositorio = new ProdutoRepositorio();
    private ControladorTelaRaiz telaRaiz;

    private Produto produtoEmEdicao;   // null = cadastro novo; preenchido = edição
    private File imagemEscolhida;      // arquivo escolhido no FileChooser, ainda não copiado
    private boolean removerImagemAtual; // marca que a imagem deve ser removida ao salvar

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) {
        this.telaRaiz = telaRaiz;
    }

    @FXML
    public void initialize() {
        atualizarPreview();
        botaoCancelarEdicao.setVisible(false); // só aparece em modo edição
        botaoCancelarEdicao.setManaged(false);
    }

    public void setProdutoParaEditar(Produto produto) { // MÉTODO: entra em modo edição
        this.produtoEmEdicao = produto;
        labelTitulo.setText("Editar Produto");
        campoNome.setText(produto.getNome());
        campoCategoria.setText(produto.getCategoria() == null ? "" : produto.getCategoria());
        campoCusto.setText(String.format("%.2f", produto.getPrecoCusto()));
        campoVenda.setText(String.format("%.2f", produto.getPrecoVenda()));
        campoEstoque.setText(String.valueOf(produto.getEstoque()));
        botaoCancelarEdicao.setVisible(true);
        botaoCancelarEdicao.setManaged(true);
        atualizarPreview();
    }

    @FXML
    public void escolherImagem() { // MÉTODO: abre o seletor de arquivo do Windows
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Escolher imagem do produto");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
        File arquivo = chooser.showOpenDialog(previewImagem.getScene().getWindow());
        if (arquivo != null) {
            imagemEscolhida = arquivo;
            removerImagemAtual = false;
            atualizarPreview();
        }
    }

    @FXML
    public void removerImagem() { // MÉTODO: marca pra remover a imagem
        imagemEscolhida = null;
        removerImagemAtual = true;
        atualizarPreview();
    }

    private void atualizarPreview() { // MÉTODO: mostra a imagem certa no preview
        Image img = null;
        if (imagemEscolhida != null) {
            img = new Image(imagemEscolhida.toURI().toString());
        } else if (!removerImagemAtual && produtoEmEdicao != null
                && GerenciadorImagens.imagemExiste(produtoEmEdicao.getImagem())) {
            File f = GerenciadorImagens.caminhoImagem(produtoEmEdicao.getImagem());
            img = new Image(f.toURI().toString());
        }
        previewImagem.setImage(img);
        boolean temImagem = (img != null);
        labelSemImagem.setVisible(!temImagem);
        labelSemImagem.setManaged(!temImagem);
        botaoRemoverImagem.setVisible(temImagem);
        botaoRemoverImagem.setManaged(temImagem);
    }

    @FXML
    public void salvar() { // MÉTODO: cadastra novo OU atualiza
        String nome = campoNome.getText();
        if (nome == null || nome.isBlank()) { mostrarAviso("Informe o nome do produto."); return; }

        double custo, venda;
        int estoque;
        try {
            custo = Double.parseDouble(campoCusto.getText().replace(",", ".").trim());
            venda = Double.parseDouble(campoVenda.getText().replace(",", ".").trim());
            estoque = Integer.parseInt(campoEstoque.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAviso("Verifique os valores de custo, venda e estoque (números válidos).");
            return;
        }
        if (custo < 0 || venda < 0 || estoque < 0) { mostrarAviso("Valores não podem ser negativos."); return; }

        String categoria = campoCategoria.getText();
        if (categoria != null && categoria.isBlank()) categoria = null;

        try {
            if (produtoEmEdicao == null) {
                // CADASTRO NOVO: salva primeiro (gera id), depois trata imagem
                Produto novo = new Produto();
                novo.setNome(nome);
                novo.setCategoria(categoria);
                novo.setPrecoCusto(custo);
                novo.setPrecoVenda(venda);
                novo.setEstoque(estoque);
                repositorio.salvar(novo); // gera o id

                if (imagemEscolhida != null) {
                    String nomeArquivo = GerenciadorImagens.copiarImagem(imagemEscolhida, novo.getId());
                    novo.setImagem(nomeArquivo);
                    repositorio.atualizar(novo); // grava o nome da imagem
                }
                mostrarAviso("Produto cadastrado com sucesso.");
            } else {
                // EDIÇÃO
                produtoEmEdicao.setNome(nome);
                produtoEmEdicao.setCategoria(categoria);
                produtoEmEdicao.setPrecoCusto(custo);
                produtoEmEdicao.setPrecoVenda(venda);
                produtoEmEdicao.setEstoque(estoque);

                if (imagemEscolhida != null) {
                    String nomeArquivo = GerenciadorImagens.copiarImagem(imagemEscolhida, produtoEmEdicao.getId());
                    produtoEmEdicao.setImagem(nomeArquivo);
                } else if (removerImagemAtual) {
                    produtoEmEdicao.setImagem(null);
                }
                repositorio.atualizar(produtoEmEdicao);
                mostrarAviso("Produto atualizado com sucesso.");
            }
            voltar();
        } catch (SQLException e) {
            mostrarAviso("Erro ao salvar: " + e.getMessage());
        } catch (java.io.IOException e) {
            mostrarAviso("Erro ao copiar a imagem: " + e.getMessage());
        }
    }

    @FXML
    public void cancelarEdicao() { // MÉTODO: descarta e volta
        voltar();
    }

    @FXML
    public void voltar() { // MÉTODO: volta pra loja
        if (telaRaiz != null) {
            telaRaiz.abrirLoja();
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