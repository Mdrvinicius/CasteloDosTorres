package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Funcionario;
import com.castelodostorres.sistema.modelo.PagamentoFuncionario;
import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.modelo.dto.ComissaoFuncionario;
import com.castelodostorres.sistema.repositorio.FuncionarioRepositorio;
import com.castelodostorres.sistema.repositorio.PagamentoFuncionarioRepositorio;
import com.castelodostorres.sistema.repositorio.VisitaRepositorio;
import com.castelodostorres.sistema.servico.CalculadoraComissao;
import com.castelodostorres.sistema.util.FormatadorData;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorTelaPagamentos implements Initializable, PrecisaDaTelaRaiz {

    // formulário
    @FXML private Label labelFormulario;
    @FXML private ComboBox<Funcionario> comboFuncionario;
    @FXML private ComboBox<Integer> comboMes;
    @FXML private TextField campoAno;
    @FXML private TextField campoValor;
    @FXML private Button botaoCancelarEdicao;

    // tabela de comissões (a pagar)
    @FXML private ComboBox<Integer> comboMesComissao;
    @FXML private TextField campoAnoComissao;
    @FXML private TableView<ComissaoFuncionario> tabelaComissoes;
    @FXML private TableColumn<ComissaoFuncionario, String> colComNome;
    @FXML private TableColumn<ComissaoFuncionario, String> colComPapel;
    @FXML private TableColumn<ComissaoFuncionario, String> colComComissao;
    @FXML private TableColumn<ComissaoFuncionario, String> colComPago;
    @FXML private TableColumn<ComissaoFuncionario, String> colComFalta;

    // tabela de pagamentos registrados
    @FXML private ComboBox<Integer> comboMesFiltro;
    @FXML private TextField campoAnoFiltro;
    @FXML private TableView<PagamentoFuncionario> tabelaPagamentos;
    @FXML private TableColumn<PagamentoFuncionario, String> colFuncionario;
    @FXML private TableColumn<PagamentoFuncionario, String> colValor;
    @FXML private TableColumn<PagamentoFuncionario, String> colData;
    @FXML private Label labelTotalPago;

    private final PagamentoFuncionarioRepositorio repositorio = new PagamentoFuncionarioRepositorio();
    private final FuncionarioRepositorio funcionarioRepositorio = new FuncionarioRepositorio();
    private final VisitaRepositorio visitaRepositorio = new VisitaRepositorio();
    private final CalculadoraComissao calculadoraComissao = new CalculadoraComissao();
    private ControladorTelaRaiz telaRaiz;

    private PagamentoFuncionario pagamentoEmEdicao; // null = registro novo

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) {
        this.telaRaiz = telaRaiz;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LocalDate hoje = LocalDate.now();
        for (int m = 1; m <= 12; m++) {
            comboMes.getItems().add(m);
            comboMesComissao.getItems().add(m);
            comboMesFiltro.getItems().add(m);
        }
        comboMes.setValue(hoje.getMonthValue());
        campoAno.setText(String.valueOf(hoje.getYear()));
        comboMesComissao.setValue(hoje.getMonthValue());
        campoAnoComissao.setText(String.valueOf(hoje.getYear()));
        comboMesFiltro.setValue(hoje.getMonthValue());
        campoAnoFiltro.setText(String.valueOf(hoje.getYear()));

        carregarFuncionarios();

        // colunas de comissão
        colComNome.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nome"));
        colComPapel.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("papel"));
        colComComissao.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getValor())));
        colComPago.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getPago())));
        colComFalta.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getFaltaPagar())));

        // colunas de pagamentos registrados
        colFuncionario.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nomeFuncionario"));
        colValor.setCellValueFactory(d -> new SimpleStringProperty("R$ " + String.format("%.2f", d.getValue().getValor())));
        colData.setCellValueFactory(d -> new SimpleStringProperty(FormatadorData.formatar(d.getValue().getDataHoraRegistro())));

        configurarCliqueDuploComissao();
        configurarCliqueDuploPagamento();

        botaoCancelarEdicao.setVisible(false);
        botaoCancelarEdicao.setManaged(false);

        gerarComissoes();
        filtrar();
    }

    private void carregarFuncionarios() {
        try {
            List<Funcionario> funcs = funcionarioRepositorio.listarTodos();
            comboFuncionario.setItems(FXCollections.observableArrayList(funcs));
            comboFuncionario.setConverter(new StringConverter<>() {
                @Override public String toString(Funcionario f) { return f == null ? "" : f.getNome(); }
                @Override public Funcionario fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            System.out.println("Erro ao carregar funcionários: " + e.getMessage());
        }
    }

    private void configurarCliqueDuploComissao() { // duplo-clique na comissão preenche o formulário
        tabelaComissoes.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                ComissaoFuncionario sel = tabelaComissoes.getSelectionModel().getSelectedItem();
                if (sel != null) preencherFormularioPelaComissao(sel);
            }
        });
    }

    private void configurarCliqueDuploPagamento() { // duplo-clique no pagamento entra em edição
        tabelaPagamentos.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                PagamentoFuncionario sel = tabelaPagamentos.getSelectionModel().getSelectedItem();
                if (sel != null) entrarEmEdicao(sel);
            }
        });
    }

    private void preencherFormularioPelaComissao(ComissaoFuncionario c) { // MÉTODO: duplo-clique na comissão -> formulário
        cancelarEdicao(); // sai de modo edição se estava
        // seleciona o funcionário pelo id
        for (Funcionario f : comboFuncionario.getItems()) {
            if (f.getId() != null && f.getId() == c.getFuncionarioId()) {
                comboFuncionario.setValue(f);
                break;
            }
        }
        // mês/ano = o do filtro da tabela de comissões
        comboMes.setValue(comboMesComissao.getValue());
        campoAno.setText(campoAnoComissao.getText());
        // sugere o valor que falta pagar
        campoValor.setText(String.format("%.2f", c.getFaltaPagar()));
    }

    private void entrarEmEdicao(PagamentoFuncionario p) {
        pagamentoEmEdicao = p;
        labelFormulario.setText("Editar Pagamento");
        for (Funcionario f : comboFuncionario.getItems()) {
            if (f.getId() != null && f.getId() == p.getFuncionarioId()) {
                comboFuncionario.setValue(f);
                break;
            }
        }
        String[] partes = p.getMesReferencia().split("-");
        if (partes.length == 2) {
            campoAno.setText(partes[0]);
            comboMes.setValue(Integer.parseInt(partes[1]));
        }
        campoValor.setText(String.format("%.2f", p.getValor()));
        botaoCancelarEdicao.setVisible(true);
        botaoCancelarEdicao.setManaged(true);
    }

    @FXML
    public void salvar() {
        Funcionario func = comboFuncionario.getValue();
        if (func == null || func.getId() == null) { mostrarAviso("Selecione um funcionário."); return; }
        Integer mes = comboMes.getValue();
        if (mes == null) { mostrarAviso("Selecione o mês de referência."); return; }
        int ano;
        try { ano = Integer.parseInt(campoAno.getText().trim()); }
        catch (NumberFormatException e) { mostrarAviso("Ano inválido."); return; }
        double valor;
        try { valor = Double.parseDouble(campoValor.getText().replace(",", ".").trim()); }
        catch (NumberFormatException e) { mostrarAviso("Valor inválido."); return; }
        if (valor <= 0) { mostrarAviso("O valor deve ser maior que zero."); return; }

        String mesRef = String.format("%04d-%02d", ano, mes);

        try {
            if (pagamentoEmEdicao == null) {
                PagamentoFuncionario p = new PagamentoFuncionario();
                p.setFuncionarioId(func.getId());
                p.setNomeFuncionario(func.getNome());
                p.setMesReferencia(mesRef);
                p.setValor(valor);
                p.setDataHoraRegistro(LocalDateTime.now().toString());
                repositorio.salvar(p);
                mostrarAviso("Pagamento registrado.");
            } else {
                pagamentoEmEdicao.setFuncionarioId(func.getId());
                pagamentoEmEdicao.setNomeFuncionario(func.getNome());
                pagamentoEmEdicao.setMesReferencia(mesRef);
                pagamentoEmEdicao.setValor(valor);
                repositorio.atualizar(pagamentoEmEdicao);
                mostrarAviso("Pagamento atualizado.");
            }
            cancelarEdicao();
            // sincroniza os filtros das tabelas pro mês registrado e atualiza tudo
            comboMesFiltro.setValue(mes);
            campoAnoFiltro.setText(String.valueOf(ano));
            comboMesComissao.setValue(mes);
            campoAnoComissao.setText(String.valueOf(ano));
            filtrar();
            gerarComissoes();
        } catch (SQLException e) {
            mostrarAviso("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    public void cancelarEdicao() {
        pagamentoEmEdicao = null;
        labelFormulario.setText("Registrar Pagamento");
        comboFuncionario.getSelectionModel().clearSelection();
        campoValor.clear();
        botaoCancelarEdicao.setVisible(false);
        botaoCancelarEdicao.setManaged(false);
    }

    @FXML
    public void gerarComissoes() { // MÉTODO: calcula as comissões do mês (com pago/falta) e mostra ordenado por nome
        Integer mes = comboMesComissao.getValue();
        if (mes == null) return;
        int ano;
        try { ano = Integer.parseInt(campoAnoComissao.getText().trim()); }
        catch (NumberFormatException e) { mostrarAviso("Ano das comissões inválido."); return; }
        String mesTexto = String.format("%04d-%02d", ano, mes);

        try {
            List<Visita> visitas = visitaRepositorio.listarDoMes(mesTexto);
            List<ComissaoFuncionario> comissoes = calculadoraComissao.calcular(visitas);

            Map<Integer, Double> pagos = repositorio.somarPorFuncionarioNoMes(mesTexto);
            for (ComissaoFuncionario c : comissoes) {
                Double pago = pagos.get(c.getFuncionarioId());
                c.setPago(pago == null ? 0.0 : pago);
            }
            comissoes.sort(Comparator.comparing(ComissaoFuncionario::getNome, String.CASE_INSENSITIVE_ORDER));
            tabelaComissoes.setItems(FXCollections.observableArrayList(comissoes));
        } catch (SQLException e) {
            mostrarAviso("Erro ao carregar comissões: " + e.getMessage());
        }
    }

    @FXML
    public void filtrar() {
        Integer mes = comboMesFiltro.getValue();
        if (mes == null) return;
        int ano;
        try { ano = Integer.parseInt(campoAnoFiltro.getText().trim()); }
        catch (NumberFormatException e) { mostrarAviso("Ano do filtro inválido."); return; }
        String mesRef = String.format("%04d-%02d", ano, mes);

        try {
            List<PagamentoFuncionario> pagamentos = repositorio.listarDoMes(mesRef);
            tabelaPagamentos.setItems(FXCollections.observableArrayList(pagamentos));
            double total = 0;
            for (PagamentoFuncionario p : pagamentos) total += p.getValor();
            labelTotalPago.setText("Total pago no mês: R$ " + String.format("%.2f", total));
        } catch (SQLException e) {
            mostrarAviso("Erro ao filtrar: " + e.getMessage());
        }
    }

    @FXML
    public void excluir() {
        PagamentoFuncionario sel = tabelaPagamentos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecione um pagamento para excluir."); return; }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Excluir Pagamento");
        conf.setHeaderText(null);
        conf.setContentText("Excluir o pagamento de R$ " + String.format("%.2f", sel.getValor()) +
                " para " + sel.getNomeFuncionario() + "?");
        Optional<ButtonType> resp = conf.showAndWait();
        if (resp.isEmpty() || resp.get() != ButtonType.OK) return;

        try {
            repositorio.excluir(sel.getId());
            mostrarAviso("Pagamento excluído.");
            filtrar();
            gerarComissoes();
        } catch (SQLException e) {
            mostrarAviso("Erro ao excluir: " + e.getMessage());
        }
    }

    private void mostrarAviso(String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(m);
        a.showAndWait();
    }
}