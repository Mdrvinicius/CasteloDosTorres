package com.castelodostorres.sistema;



import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException { // "throws IOException": carregar arquivo pode falhar
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/castelodostorres/sistema/TelaRaiz.fxml"));
        Parent raiz = loader.load();

        stage.setTitle("Castelo dos Torres - Cadastro de Funcionário");

        Scene scene = new Scene(raiz, 1220, 700);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }




    public static void main(String[] args) {
        launch(args);
    }

}
