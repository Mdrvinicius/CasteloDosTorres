package com.castelodostorres.sistema;



import com.castelodostorres.sistema.modelo.SentidoCatraca;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import com.castelodostorres.sistema.modelo.ConfiguracaoConexao;
import com.castelodostorres.sistema.servico.ServicoControlId;

import java.io.IOException;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException { // "throws IOException": carregar arquivo pode falhar
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/castelodostorres/sistema/TelaRaiz.fxml"));
        Parent raiz = loader.load();

        stage.setTitle("Castelo dos Torres - Cadastro de Funcionário");

        Scene scene = new Scene(raiz, 1220, 700);
        stage.setScene(scene);
        stage.show();
    }




    public static void main(String[] args) {
        launch(args);
    }

}
