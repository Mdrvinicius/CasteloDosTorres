package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Configuracao;
import com.castelodostorres.sistema.repositorio.ConfiguracaoRepositorio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ControladorTelaConfiguracao implements Initializable {

    @FXML private TextField campoIp;             // ATRIBUTO: representa o TextField do FXML com fx:id="campoIp"
    @FXML private TextField campoPorta;           // ATRIBUTO: representa fx:id="campoPorta"
    @FXML private TextField campoUsuario;          // ATRIBUTO: representa fx:id="campoUsuario"
    @FXML private PasswordField campoSenha;         // ATRIBUTO: representa fx:id="campoSenha"
    @FXML private TextField campoValorInteira;       // ATRIBUTO: representa fx:id="campoValorInteira"
    @FXML private TextField campoValorMeia;           // ATRIBUTO: representa fx:id="campoValorMeia"
    @FXML private PasswordField campoSenhaAdmin;


    private final ConfiguracaoRepositorio repositorio = new ConfiguracaoRepositorio();
    // ATRIBUTO: uma única instância do Repository, criada uma vez, reutilizada pelos métodos abaixo
    // (mesmo princípio do "único HttpClient" e "único ObjectMapper" que já vimos antes)

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // MÉTODO: chamado automaticamente ao abrir a tela
        carregarConfiguracaoExistente();
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Aviso");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
    private void carregarConfiguracaoExistente() { // MÉTODO: busca no banco e preenche os campos, se já existir algo salvo
        try {
            Configuracao configuracao = repositorio.buscar();

            if (configuracao != null) { // se já existe uma configuração salva, preenche os campos com ela
                campoIp.setText(configuracao.getIpCatraca());
                campoPorta.setText(String.valueOf(configuracao.getPortaCatraca()));
                campoUsuario.setText(configuracao.getUsuarioCatraca());
                campoSenha.setText(configuracao.getSenhaCatraca());
                campoValorInteira.setText(String.valueOf(configuracao.getValorInteira()));
                campoValorMeia.setText(String.valueOf(configuracao.getValorMeia()));

            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar configuração: " + e.getMessage());
        }
    }

    @FXML
    public void salvar() { // MÉTODO: chamado pelo clique do botão "Salvar Configuração" (onAction="#salvar" no FXML)
        String ip = campoIp.getText();
        String usuario = campoUsuario.getText();
        String senha = campoSenha.getText();

        int porta;
        try {
            porta = Integer.parseInt(campoPorta.getText());
        } catch (NumberFormatException e) {
            System.out.println("Porta inválida, digite apenas números.");
            return;
        }

        double valorInteira;
        double valorMeia;
        try {
            valorInteira = Double.parseDouble(campoValorInteira.getText().replace(",", "."));
            valorMeia = Double.parseDouble(campoValorMeia.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Valor de inteira/meia inválido, digite apenas números.");
            return;
        }



        Configuracao configuracao = new Configuracao();
        configuracao.setIpCatraca(ip);
        configuracao.setPortaCatraca(porta);
        configuracao.setUsuarioCatraca(usuario);
        configuracao.setSenhaCatraca(senha);
        configuracao.setValorInteira(valorInteira);
        configuracao.setValorMeia(valorMeia);

        try {
            repositorio.salvar(configuracao);

            String senhaDigitada = campoSenhaAdmin.getText();
            if (senhaDigitada != null && !senhaDigitada.isBlank()) {
                repositorio.salvarSenhaAdmin(senhaDigitada);
            }

            mostrarAviso("Configuração salva com sucesso!");
        } catch (SQLException e) {
            mostrarAviso("Erro ao salvar configuração: " + e.getMessage());
        }
    }
}