package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.repositorio.VisitaRepositorio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorTelaVisitas implements Initializable, PrecisaDaTelaRaiz {

    @FXML private TextField campoBusca;
    @FXML private TextField campoData;
    @FXML private TableView<Visita> tabelaVisitas;
    @FXML private TableColumn<Visita, String> colunaData;
    @FXML private TableColumn<Visita, String> colunaGuia;
    @FXML private TableColumn<Visita, String> colunaRecepcionista;
    @FXML private TableColumn<Visita, Integer> colunaInteiras;
    @FXML private TableColumn<Visita, Integer> colunaMeias;
    @FXML private TableColumn<Visita, Integer> colunaNaoPagantes;
    @FXML private TableColumn<Visita, Double> colunaTotal;
    @FXML private TableColumn<Visita, String> colunaStatus;

    private final VisitaRepositorio repositorio = new VisitaRepositorio();

    private ControladorTelaRaiz telaRaiz;

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) { // MÉTODO da interface: recebe a TelaRaiz
        this.telaRaiz = telaRaiz;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarColunas();
        carregarTodas();
        configurarCliqueDuplo();
    }

    private void configurarCliqueDuplo() { // MÉTODO: abre Detalhes ao dar duplo-clique numa linha
        tabelaVisitas.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) { // 2 = duplo-clique
                Visita selecionada = tabelaVisitas.getSelectionModel().getSelectedItem();
                if (selecionada != null && telaRaiz != null) {
                    telaRaiz.abrirDetalhesVisita(selecionada);
                }
            }
        });
    }


        private void configurarColunas() { // MÉTODO: diz a cada coluna qual atributo da Visita ela exibe
        colunaData.setCellValueFactory(new PropertyValueFactory<>("dataHoraInicio"));
        colunaGuia.setCellValueFactory(new PropertyValueFactory<>("nomeGuia"));
        colunaRecepcionista.setCellValueFactory(new PropertyValueFactory<>("nomeRecepcionista"));
        colunaInteiras.setCellValueFactory(new PropertyValueFactory<>("quantidadeInteira"));
        colunaMeias.setCellValueFactory(new PropertyValueFactory<>("quantidadeMeia"));
        colunaNaoPagantes.setCellValueFactory(new PropertyValueFactory<>("quantidadeNaoPagante"));
        colunaTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        colunaStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void carregarTodas() { // MÉTODO: busca todas as visitas e joga na tabela
        try {
            List<Visita> visitas = repositorio.listarTodas();
            tabelaVisitas.setItems(FXCollections.observableArrayList(visitas));
        } catch (SQLException e) {
            System.out.println("Erro ao carregar visitas: " + e.getMessage());
        }
    }



    @FXML
    public void buscar() { // MÉTODO: por enquanto placeholder, implementamos o filtro no próximo passo
        System.out.println("Busca: guia=" + campoBusca.getText() + ", data=" + campoData.getText());
    }

    @FXML
    public void limpar() { // MÉTODO: limpa os campos e recarrega tudo
        campoBusca.clear();
        campoData.clear();
        carregarTodas();
    }


}