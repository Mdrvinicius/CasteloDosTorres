package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Funcionario;
import com.castelodostorres.sistema.repositorio.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorTelaCaixa implements Initializable {

    @FXML private DatePicker seletorData;
    @FXML private TextField campoFundoTroco;
    @FXML private Label labelDinheiroEsperado;
    @FXML private Label labelDetalheDinheiro;
    @FXML private TextField campoDinheiroContado;
    @FXML private Label labelDiferencaDinheiro;
    @FXML private Label labelPixDebitoEsperado;
    @FXML private TextField campoPixDebitoContado;
    @FXML private Label labelDiferencaPixDebito;
    @FXML private Label labelTotalSaidas;
    @FXML private TextField campoValorSaida;
    @FXML private TextField campoMotivoSaida;
    @FXML private Label labelAgendados;
    @FXML private Label labelReembolsos;
    @FXML private ComboBox<Funcionario> comboFuncionario;
    @FXML private TextField campoEntregue;
    @FXML private TextField campoEmCaixa;

    private final VisitaRepositorio visitaRepositorio = new VisitaRepositorio();
    private final FundoTrocoRepositorio fundoTrocoRepositorio = new FundoTrocoRepositorio();
    private final SaidaCaixaRepositorio saidaCaixaRepositorio = new SaidaCaixaRepositorio();
    private final FechamentoCaixaRepositorio fechamentoRepositorio = new FechamentoCaixaRepositorio();
    private final FuncionarioRepositorio funcionarioRepositorio = new FuncionarioRepositorio();
    private final VendaRepositorio vendaRepositorio = new VendaRepositorio();


    // guardo os esperados calculados, pra usar no salvar sem recalcular
    private double dinheiroEsperado;
    private double pixDebitoEsperado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        seletorData.setValue(LocalDate.now());
        carregarFuncionarios();

        // recalcula a diferença conforme digita
        campoDinheiroContado.textProperty().addListener((o, a, n) -> atualizarDiferencaDinheiro());
        campoPixDebitoContado.textProperty().addListener((o, a, n) -> atualizarDiferencaPixDebito());

        gerar();
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

    @FXML
    public void gerar() {
        LocalDate data = seletorData.getValue();
        if (data == null) { mostrarAviso("Selecione uma data."); return; }
        String dataTexto = data.toString();

        try {
            // fundo: se JÁ FOI SALVO hoje (mesmo que 0), respeita o salvo; só herda se não há registro nenhum
            Double fundoSalvo = fundoTrocoRepositorio.buscarOuNulo(dataTexto);
            double fundo;
            if (fundoSalvo != null) {
                fundo = fundoSalvo; // ela já salvou hoje — respeita, inclusive 0 intencional
            } else {
                String ontem = data.minusDays(1).toString();
                Double emCaixaOntem = fechamentoRepositorio.buscarEmCaixaDoDia(ontem);
                fundo = (emCaixaOntem != null) ? emCaixaOntem : 0.0;
            }
            campoFundoTroco.setText(String.format("%.2f", fundo));

            double[] esperadoNaoAgendadas = visitaRepositorio.calcularEsperadoNaoAgendadas(dataTexto);
            double dinheiroVendas = esperadoNaoAgendadas[0];
            double pixDebitoVendas = esperadoNaoAgendadas[1];

            double[] formasLoja = vendaRepositorio.calcularFormasPagamentoDoDia(dataTexto);
            double dinheiroLoja = formasLoja[0];
            double pixDebitoLoja = formasLoja[1] + formasLoja[2]; // pix + débito juntos

            double totalSaidas = saidaCaixaRepositorio.totalDoDia(dataTexto);
            labelTotalSaidas.setText("Total de saídas do dia: R$ " + String.format("%.2f", totalSaidas));

            // dinheiro esperado = fundo + vendas em dinheiro - saídas
            dinheiroEsperado = fundo + dinheiroVendas + dinheiroLoja - totalSaidas;
            labelDinheiroEsperado.setText("Esperado: R$ " + String.format("%.2f", dinheiroEsperado));
            labelDetalheDinheiro.setText("(fundo R$ " + String.format("%.2f", fundo) +
                    " + visitas dinheiro R$ " + String.format("%.2f", dinheiroVendas) +
                    " + loja dinheiro R$ " + String.format("%.2f", dinheiroLoja) +
                    " − saídas R$ " + String.format("%.2f", totalSaidas) + ")");

            // pix+débito esperado = vendas em pix+débito
            pixDebitoEsperado = pixDebitoVendas + pixDebitoLoja;
            labelPixDebitoEsperado.setText("Esperado: R$ " + String.format("%.2f", pixDebitoEsperado));

            double reembolsos = visitaRepositorio.calcularReembolsosDoDia(dataTexto);
            labelReembolsos.setText("Reembolsos do dia: R$ " + String.format("%.2f", reembolsos));

            // agendados (informativo)
            double[] ag = visitaRepositorio.calcularAgendadosDoDia(dataTexto);
            labelAgendados.setText("Valor de agendados: dinheiro R$ " + String.format("%.2f", ag[0]) +
                    " | pix R$ " + String.format("%.2f", ag[1]) +
                    " | débito R$ " + String.format("%.2f", ag[2]));

            atualizarDiferencaDinheiro();
            atualizarDiferencaPixDebito();
        } catch (SQLException e) {
            mostrarAviso("Erro ao gerar caixa: " + e.getMessage());
        }
    }

    private void atualizarDiferencaDinheiro() {
        double contado = lerCampo(campoDinheiroContado);
        double dif = contado - dinheiroEsperado;
        labelDiferencaDinheiro.setText("Diferença: R$ " + String.format("%.2f", dif) + statusDif(dif));
    }

    private void atualizarDiferencaPixDebito() {
        double contado = lerCampo(campoPixDebitoContado);
        double dif = contado - pixDebitoEsperado;
        labelDiferencaPixDebito.setText("Diferença: R$ " + String.format("%.2f", dif) + statusDif(dif));
    }

    private String statusDif(double dif) {
        if (Math.abs(dif) < 0.001) return " (bateu)";
        return dif < 0 ? " (faltou)" : " (sobrou)";
    }

    @FXML
    public void salvarFundo() {
        LocalDate data = seletorData.getValue();
        if (data == null) { mostrarAviso("Selecione uma data."); return; }
        double valor;
        try { valor = Double.parseDouble(campoFundoTroco.getText().replace(",", ".").trim()); }
        catch (NumberFormatException e) { mostrarAviso("Fundo inválido."); return; }
        if (valor < 0) { mostrarAviso("Fundo não pode ser negativo."); return; }
        try {
            fundoTrocoRepositorio.salvar(data.toString(), valor);
            mostrarAviso("Fundo salvo.");
            gerar(); // recalcula com o novo fundo
        } catch (SQLException e) { mostrarAviso("Erro: " + e.getMessage()); }
    }

    @FXML
    public void registrarSaida() {
        LocalDate data = seletorData.getValue();
        if (data == null) { mostrarAviso("Selecione uma data."); return; }
        double valor;
        try { valor = Double.parseDouble(campoValorSaida.getText().replace(",", ".").trim()); }
        catch (NumberFormatException e) { mostrarAviso("Valor inválido."); return; }
        if (valor <= 0) { mostrarAviso("Valor deve ser maior que zero."); return; }
        String motivo = campoMotivoSaida.getText();
        if (motivo == null || motivo.isBlank()) { mostrarAviso("Informe o motivo."); return; }
        try {
            saidaCaixaRepositorio.salvar(data.toString(), valor, motivo);
            campoValorSaida.clear();
            campoMotivoSaida.clear();
            mostrarAviso("Saída registrada.");
            gerar(); // recalcula (saída reduz o esperado de dinheiro)
        } catch (SQLException e) { mostrarAviso("Erro: " + e.getMessage()); }
    }

    @FXML
    public void salvarFechamento() {
        LocalDate data = seletorData.getValue();
        if (data == null) { mostrarAviso("Selecione uma data."); return; }
        Funcionario func = comboFuncionario.getValue();
        if (func == null) { mostrarAviso("Selecione quem está fechando o caixa."); return; }

        double dinheiroContado = lerCampo(campoDinheiroContado);
        double pixDebitoContado = lerCampo(campoPixDebitoContado);
        double entregue = lerCampo(campoEntregue);
        double emCaixa = lerCampo(campoEmCaixa);

        // validação: entregue + em caixa tem que bater com o dinheiro CONTADO
        if (Math.abs((entregue + emCaixa) - dinheiroContado) > 0.001) {
            mostrarAviso("A soma de Entregue (R$ " + String.format("%.2f", entregue) +
                    ") + Em caixa (R$ " + String.format("%.2f", emCaixa) +
                    ") = R$ " + String.format("%.2f", entregue + emCaixa) +
                    " não bate com o Dinheiro contado (R$ " + String.format("%.2f", dinheiroContado) + ").");
            return;
        }

        try {
            // fundo real de hoje = o que está salvo na fundo_troco (o que ela confirmou na abertura)
            double fundoReal = fundoTrocoRepositorio.buscar(data.toString());

            // fundo herdado = em caixa do último fechamento de ONTEM (null se ontem não fechou)
            String ontem = data.minusDays(1).toString();
            Double emCaixaOntem = fechamentoRepositorio.buscarEmCaixaDoDia(ontem);
            boolean temFundoHerdado = (emCaixaOntem != null);
            double fundoHerdado = temFundoHerdado ? emCaixaOntem : 0.0;

            fechamentoRepositorio.salvar(
                    data.toString(),
                    LocalDateTime.now().toString(),
                    func.getId(),
                    func.getNome(),
                    dinheiroEsperado, dinheiroContado,
                    pixDebitoEsperado, pixDebitoContado,
                    entregue, emCaixa,
                    fundoHerdado, fundoReal, temFundoHerdado
            );

            double difD = dinheiroContado - dinheiroEsperado;
            double difP = pixDebitoContado - pixDebitoEsperado;
            StringBuilder aviso = new StringBuilder("Fechamento salvo.\n");
            aviso.append("Divergência dinheiro: R$ ").append(String.format("%.2f", difD)).append(statusDif(difD)).append("\n");
            aviso.append("Divergência pix+débito: R$ ").append(String.format("%.2f", difP)).append(statusDif(difP)).append("\n");
            if (temFundoHerdado) {
                double difFundo = fundoReal - fundoHerdado;
                aviso.append("Fundo: esperado R$ ").append(String.format("%.2f", fundoHerdado))
                        .append(", abriu com R$ ").append(String.format("%.2f", fundoReal))
                        .append(Math.abs(difFundo) < 0.001 ? " (correto)" : (difFundo < 0 ? " (faltou)" : " (sobrou)"));
            } else {
                aviso.append("Fundo iniciado manualmente.");
            }
            mostrarAviso(aviso.toString());

            // limpa a tela após fechar
            campoDinheiroContado.clear();
            campoPixDebitoContado.clear();
            campoFundoTroco.clear();
            campoEntregue.clear();
            campoEmCaixa.clear();
            comboFuncionario.getSelectionModel().clearSelection();
            labelDinheiroEsperado.setText("Esperado: R$ 0,00");
            labelDetalheDinheiro.setText("");
            labelPixDebitoEsperado.setText("Esperado: R$ 0,00");
            labelDiferencaDinheiro.setText("Diferença: R$ 0,00");
            labelDiferencaPixDebito.setText("Diferença: R$ 0,00");
            labelTotalSaidas.setText("Total de saídas do dia: R$ 0,00");
            labelAgendados.setText("Valor de agendados: —");
            labelReembolsos.setText("Reembolsos do dia: R$ 0,00");
            dinheiroEsperado = 0;
            pixDebitoEsperado = 0;
        } catch (SQLException e) { mostrarAviso("Erro ao salvar fechamento: " + e.getMessage()); }
    }

    private double lerCampo(TextField campo) {
        try {
            String t = campo.getText().replace(",", ".").trim();
            return t.isBlank() ? 0.0 : Double.parseDouble(t);
        } catch (NumberFormatException e) { return 0.0; }
    }

    private void mostrarAviso(String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(m);
        a.showAndWait();
    }
}