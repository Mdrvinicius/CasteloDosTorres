package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Funcionario;
import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.repositorio.FuncionarioRepositorio;
import com.castelodostorres.sistema.repositorio.VisitaRepositorio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorTelaVisitas implements Initializable, PrecisaDaTelaRaiz {

    @FXML private ComboBox<Funcionario> comboGuiaBusca;
    @FXML private DatePicker seletorDataBusca;
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
        carregarGuiasNoComboBusca();
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
    public void buscar() { // MÉTODO: filtra as visitas por guia e/ou data
        Funcionario guiaSelecionada = comboGuiaBusca.getValue();
        Integer guiaId = (guiaSelecionada == null) ? null : guiaSelecionada.getId();

        LocalDate data = seletorDataBusca.getValue();
        String dataTexto = (data == null) ? null : data.toString();

        try {
            List<Visita> resultado = repositorio.buscar(guiaId, dataTexto);
            tabelaVisitas.setItems(FXCollections.observableArrayList(resultado));
        } catch (SQLException e) {
            System.out.println("Erro na busca: " + e.getMessage());
        }
    }

    @FXML
    public void limpar() {
        comboGuiaBusca.getSelectionModel().clearSelection();
        seletorDataBusca.setValue(null);
        carregarTodas();
    }

    private void carregarGuiasNoComboBusca() { // MÉTODO: enche o combo de busca com as guias
        FuncionarioRepositorio funcRepositorio = new FuncionarioRepositorio();
        try {
            List<Funcionario> guias = funcRepositorio.listarPorPapel("GUIA");
            comboGuiaBusca.setItems(FXCollections.observableArrayList(guias));
            comboGuiaBusca.setConverter(new StringConverter<>() {
                @Override
                public String toString(Funcionario f) {
                    return f == null ? "" : f.getNome();
                }
                @Override
                public Funcionario fromString(String texto) {
                    return null;
                }
            });
        } catch (SQLException e) {
            System.out.println("Erro ao carregar guias: " + e.getMessage());
        }
    }


}