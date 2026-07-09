package com.castelodostorres.sistema;



import javafx.application.Application;
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


public class Main extends Application {

    public static void main(String[] args){
        launch(args);
    }
    @Override

    public void start(Stage stage){
        stage.setTitle("Castelo dos Torres");

        TextField campoIp = new TextField("192.168.3.165");
        TextField campoPorta = new TextField("80");
        TextField campoUsuario = new TextField("admin");
        PasswordField campoSenha = new PasswordField();

        Button botaoConectar = new Button("Conectar");

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(8);
        formulario.setPadding(new Insets(15));

        formulario.addRow(0, new Label("IP da catraca:"), campoIp);
        formulario.addRow(1, new Label("Porta:"), campoPorta);
        formulario.addRow(2, new Label("Usuário:"), campoUsuario);
        formulario.addRow(3, new Label("Senha:"), campoSenha);
        formulario.addRow(4, botaoConectar);

        botaoConectar.setOnAction(evento -> {
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

            ConfiguracaoConexao config = new ConfiguracaoConexao(ip, porta, usuario, senha);
            System.out.println("Configuração criada: " + config.getIp() + ":" + config.getPorta());


            System.out.println("IP: " + ip );
            System.out.println("Porta: " + porta);
            System.out.println("USUARIO " + usuario);
            System.out.println("SENHA " + senha);
        });



        Scene scene = new Scene(formulario, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

}
