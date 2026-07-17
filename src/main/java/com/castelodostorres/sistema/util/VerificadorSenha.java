package com.castelodostorres.sistema.util;

import com.castelodostorres.sistema.repositorio.ConfiguracaoRepositorio;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputDialog;

import java.sql.SQLException;
import java.util.Optional;

public class VerificadorSenha {

    public static boolean verificar() { // MÉTODO estático: pede a senha e diz se pode prosseguir
        ConfiguracaoRepositorio repositorio = new ConfiguracaoRepositorio();

        String senhaSalva;
        try {
            senhaSalva = repositorio.buscarSenhaAdmin();
        } catch (SQLException e) {
            mostrarErro("Erro ao verificar senha: " + e.getMessage());
            return false;
        }

        if (senhaSalva == null || senhaSalva.isBlank()) { // nenhuma senha definida ainda: libera
            return true;
        }

        // pede a senha ao usuário
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Acesso Restrito");
        dialogo.setHeaderText("Digite a senha de administrador:");
        dialogo.setContentText("Senha:");

        // troca o campo de texto normal por um campo de senha (mascarado)
        PasswordField campoSenha = new PasswordField();
        dialogo.getDialogPane().setContent(campoSenha);

        Optional<String> resultado = dialogo.showAndWait();

        if (resultado.isEmpty()) { // cancelou
            return false;
        }

        String digitada = campoSenha.getText();
        if (senhaSalva.equals(digitada)) {
            return true;
        } else {
            mostrarErro("Senha incorreta.");
            return false;
        }
    }

    private static void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}