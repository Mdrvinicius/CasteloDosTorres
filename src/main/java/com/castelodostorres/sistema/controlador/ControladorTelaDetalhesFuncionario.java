package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Funcionario;
import com.castelodostorres.sistema.repositorio.FuncionarioRepositorio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.Optional;

public class ControladorTelaDetalhesFuncionario implements PrecisaDaTelaRaiz {

    @FXML private TextField campoNome;
    @FXML private ComboBox<String> comboPapel;
    @FXML private ComboBox<String> comboTipoRemuneracao;
    @FXML private TextField campoValor;

    private final FuncionarioRepositorio repositorio = new FuncionarioRepositorio();
    private ControladorTelaRaiz telaRaiz;
    private Funcionario funcionario; // o funcionário sendo editado

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) {
        this.telaRaiz = telaRaiz;
    }

    public void setFuncionario(Funcionario funcionario) { // MÉTODO: recebe o funcionário e preenche os campos
        this.funcionario = funcionario;
        popularCombos();
        preencherCampos();
    }

    private void popularCombos() { // MÉTODO: enche os combos com as opções (igual no cadastro)
        comboPapel.setItems(FXCollections.observableArrayList("GUIA", "RECEPCIONISTA"));
        comboTipoRemuneracao.setItems(FXCollections.observableArrayList("PERCENTUAL", "FIXO_POR_PESSOA"));

        comboPapel.setConverter(criarConversor("GUIA", "Guia", "RECEPCIONISTA", "Recepcionista"));
        comboTipoRemuneracao.setConverter(criarConversor("PERCENTUAL", "Percentual (%)", "FIXO_POR_PESSOA", "Valor Fixo por Pessoa (R$)"));
    }

    private StringConverter<String> criarConversor(String v1, String r1, String v2, String r2) { // MÉTODO: converte valor técnico -> texto amigável
        return new StringConverter<>() {
            @Override
            public String toString(String valor) {
                if (valor == null) return "";
                if (valor.equals(v1)) return r1;
                if (valor.equals(v2)) return r2;
                return valor;
            }
            @Override
            public String fromString(String texto) {
                return texto;
            }
        };
    }

    private void preencherCampos() { // MÉTODO: joga os dados do funcionário nos campos
        campoNome.setText(funcionario.getNome());
        comboPapel.setValue(funcionario.getPapel());
        comboTipoRemuneracao.setValue(funcionario.getTipoRemuneracao());
        campoValor.setText(String.format("%.2f", funcionario.getValorRemuneracao()));
    }

    @FXML
    public void salvarAlteracoes() { // MÉTODO: valida e salva as edições
        String nome = campoNome.getText();
        String papel = comboPapel.getValue();
        String tipo = comboTipoRemuneracao.getValue();

        if (nome == null || nome.isBlank()) {
            mostrarAviso("Informe o nome.");
            return;
        }
        if (papel == null) {
            mostrarAviso("Selecione a função.");
            return;
        }
        if (tipo == null) {
            mostrarAviso("Selecione o tipo de remuneração.");
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(campoValor.getText().replace(",", ".").trim());
        } catch (NumberFormatException e) {
            mostrarAviso("Valor inválido.");
            return;
        }

        funcionario.setNome(nome);
        funcionario.setPapel(papel);
        funcionario.setTipoRemuneracao(tipo);
        funcionario.setValorRemuneracao(valor);

        try {
            repositorio.atualizar(funcionario);
            mostrarAviso("Alterações salvas com sucesso.");
        } catch (SQLException e) {
            mostrarAviso("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    public void voltar() { // MÉTODO: volta pra tela de funcionários
        if (telaRaiz != null) {
            telaRaiz.abrirFuncionarios();
        }
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Aviso");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    @FXML
    public void excluirFuncionario() { // MÉTODO: soft delete com confirmação
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Excluir Funcionário");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Tem certeza que deseja excluir " + funcionario.getNome() +
                "? Ele deixará de aparecer nas listas, mas o histórico de visitas será preservado.");

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.OK) {
            return; // cancelou
        }

        try {
            repositorio.desativar(funcionario.getId());
            mostrarAviso("Funcionário excluído com sucesso.");
            if (telaRaiz != null) {
                telaRaiz.abrirFuncionarios(); // volta pra lista (que já não vai mostrar ele)
            }
        } catch (SQLException e) {
            mostrarAviso("Erro ao excluir: " + e.getMessage());
        }
    }
}