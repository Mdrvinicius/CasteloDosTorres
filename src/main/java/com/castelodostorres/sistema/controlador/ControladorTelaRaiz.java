package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.util.VerificadorSenha;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class ControladorTelaRaiz {

    @FXML private BorderPane painelRaiz; // ATRIBUTO: o BorderPane inteiro, pra podermos trocar o center dele
    @FXML private javafx.scene.control.Button botaoTelaPrincipal;
    @FXML private javafx.scene.control.Button botaoCaixa;
    @FXML private javafx.scene.control.Button botaoVisitas;
    @FXML private javafx.scene.control.Button botaoRelatorios;
    @FXML private javafx.scene.control.Button botaoResumoMes;
    @FXML private javafx.scene.control.Button botaoDespesas;
    @FXML private javafx.scene.control.Button botaoFuncionarios;
    @FXML private javafx.scene.control.Button botaoConfiguracao;

    @FXML
    public void abrirTelaPrincipal() { // MÉTODO: chamado pelo botão "Tela Principal"
        trocarConteudo("/com/castelodostorres/sistema/TelaPrincipal.fxml");
        marcarAtivo(botaoTelaPrincipal);
    }

    @FXML
    public void abrirFuncionarios() { // MÉTODO: por enquanto abre o cadastro atual
        trocarConteudo("/com/castelodostorres/sistema/CadastroFuncionario.fxml");
        marcarAtivo(botaoFuncionarios);
    }

    @FXML
    public void abrirConfiguracao() {
        if (VerificadorSenha.verificar()) { // só abre se a senha for aceita (ou se não houver senha definida)
            trocarConteudo("/com/castelodostorres/sistema/TelaConfiguracao.fxml");
            marcarAtivo(botaoConfiguracao);
        }
    }

    @FXML
    public void abrirVisitas() {
        trocarConteudo("/com/castelodostorres/sistema/TelaVisitas.fxml");
        marcarAtivo(botaoVisitas);
    }

    @FXML
    public void abrirRelatorios() {
        if (VerificadorSenha.verificar()) {
            trocarConteudo("/com/castelodostorres/sistema/TelaResumoDia.fxml");
            marcarAtivo(botaoRelatorios);
        }
    }
    @FXML
    public void abrirResumoMes() {
        if (VerificadorSenha.verificar()) {
            trocarConteudo("/com/castelodostorres/sistema/TelaResumoMes.fxml");
            marcarAtivo(botaoResumoMes);
        }
    }

    private void trocarConteudo(String caminhoFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Parent tela = loader.load();

            Object controlador = loader.getController(); // pega o Controller que o FXML acabou de criar
            if (controlador instanceof PrecisaDaTelaRaiz) { // se esse Controller implementa a interface...
                ((PrecisaDaTelaRaiz) controlador).setTelaRaiz(this); // ...entrega a referência da TelaRaiz (this) pra ele
            }

            painelRaiz.setCenter(tela);
        } catch (IOException e) {
            System.out.println("Erro ao abrir tela: " + e.getMessage());
        }
    }
    public void abrirDetalhesVisita(com.castelodostorres.sistema.modelo.Visita visita) { // MÉTODO: abre Detalhes passando a visita
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/castelodostorres/sistema/TelaDetalhesVisita.fxml"));
            Parent tela = loader.load();

            ControladorTelaDetalhesVisita controlador = loader.getController();
            controlador.setTelaRaiz(this);   // pra ela conseguir voltar
            controlador.setVisita(visita);    // entrega a visita a ser mostrada

            painelRaiz.setCenter(tela);
        } catch (IOException e) {
            System.out.println("Erro ao abrir detalhes: " + e.getMessage());
        }
    }

    private void mostrarEmConstrucao(String nome) { // MÉTODO: placeholder pras telas que faremos depois
        painelRaiz.setCenter(new Label("Tela de " + nome + " em construção."));
    }

    public void abrirDetalhesFuncionario(com.castelodostorres.sistema.modelo.Funcionario funcionario) { // MÉTODO: abre detalhes passando o funcionário
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/castelodostorres/sistema/TelaDetalhesFuncionario.fxml"));
            Parent tela = loader.load();

            ControladorTelaDetalhesFuncionario controlador = loader.getController();
            controlador.setTelaRaiz(this);
            controlador.setFuncionario(funcionario);

            painelRaiz.setCenter(tela);
        } catch (IOException e) {
            System.out.println("Erro ao abrir detalhes do funcionário: " + e.getMessage());
        }
    }

    @FXML
    public void abrirDespesas() {
        trocarConteudo("/com/castelodostorres/sistema/TelaDespesas.fxml");
        marcarAtivo(botaoDespesas);
    }

    @FXML
    public void abrirCaixa() {
        trocarConteudo("/com/castelodostorres/sistema/TelaCaixa.fxml");
        marcarAtivo(botaoCaixa);
    }

    private void marcarAtivo(javafx.scene.control.Button ativo) {
        System.out.println("marcarAtivo chamado com: " + (ativo == null ? "NULL" : ativo.getText()));
        javafx.scene.control.Button[] todos = {
                botaoTelaPrincipal, botaoCaixa, botaoVisitas, botaoRelatorios,
                botaoResumoMes, botaoDespesas, botaoFuncionarios, botaoConfiguracao
        };
        for (javafx.scene.control.Button b : todos) {
            System.out.println("  botao no array: " + (b == null ? "NULL" : b.getText()));
            if (b != null) {
                b.getStyleClass().remove("nav-botao-ativo");
            }
        }

    }
}