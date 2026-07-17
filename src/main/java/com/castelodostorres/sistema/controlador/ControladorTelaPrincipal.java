package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.SentidoCatraca;
import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.repositorio.VisitaRepositorio;
import java.time.LocalDateTime;


import com.castelodostorres.sistema.modelo.Configuracao;
import com.castelodostorres.sistema.modelo.Funcionario;
import com.castelodostorres.sistema.repositorio.ConfiguracaoRepositorio;
import com.castelodostorres.sistema.repositorio.FuncionarioRepositorio;
import com.castelodostorres.sistema.servico.ServicoControlId;
import com.castelodostorres.sistema.modelo.SentidoCatraca;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javafx.scene.control.Alert;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;



public class ControladorTelaPrincipal implements Initializable, PrecisaDaTelaRaiz {

    @FXML private ComboBox<Funcionario> comboGuia;
    @FXML private ComboBox<Funcionario> comboRecepcionista;
    @FXML private Label labelInteira;
    @FXML private Label labelMeia;
    @FXML private Label labelNaoPagante;
    @FXML private TextArea campoObservacoes;
    @FXML private Label labelValorTotal;
    @FXML private TextField campoDinheiro;
    @FXML private TextField campoPix;
    @FXML private TextField campoDebito;
    @FXML private Label labelSomaPagamento;
    @FXML private Label labelUltimaGuia;
    @FXML private Label labelUltimaDataHora;
    @FXML private Label labelUltimaInteiras;
    @FXML private Label labelUltimaMeias;
    @FXML private Label labelUltimaNaoPagantes;
    @FXML private Button botaoDetalhesUltima;

    private int quantidadeInteira = 0; // ATRIBUTO: guarda o estado atual do contador
    private int quantidadeMeia = 0;
    private int quantidadeNaoPagante = 0;
    private double valorInteira; // ATRIBUTO: por enquanto fixo, depois vem da tabela configuracao
    private double valorMeia ;
    private final ServicoControlId servicoControlId = new ServicoControlId();
    private final VisitaRepositorio visitaRepositorio = new VisitaRepositorio();
    private Visita ultimaVisita; // guarda a última visita, pra abrir os detalhes dela
    private ControladorTelaRaiz telaRaiz;

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) {
        this.telaRaiz = telaRaiz;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        carregarCombos();
        carregarValores();
        configurarCatraca();
        configurarSomaPagamento();
        carregarUltimaVisita();
    }

    private void carregarUltimaVisita() { // MÉTODO: busca a última visita e mostra no painel
        try {
            ultimaVisita = visitaRepositorio.buscarUltima();
            if (ultimaVisita != null) {
                labelUltimaGuia.setText("Guia: " + ultimaVisita.getNomeGuia());
                labelUltimaDataHora.setText("Data/Hora: " + ultimaVisita.getDataHoraInicio());
                labelUltimaInteiras.setText("Inteiras: " + ultimaVisita.getQuantidadeInteira());
                labelUltimaMeias.setText("Meias: " + ultimaVisita.getQuantidadeMeia());
                labelUltimaNaoPagantes.setText("Não Pagantes: " + ultimaVisita.getQuantidadeNaoPagante());
                botaoDetalhesUltima.setDisable(false);
            } else {
                labelUltimaGuia.setText("Nenhuma visita registrada ainda.");
                botaoDetalhesUltima.setDisable(true); // desabilita o botão se não há visita
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar última visita: " + e.getMessage());
        }
    }

    @FXML
    public void abrirDetalhesUltima() { // MÉTODO: abre os detalhes da última visita
        if (ultimaVisita != null && telaRaiz != null) {
            telaRaiz.abrirDetalhesVisita(ultimaVisita);
        }
    }

    private void carregarCombos() { // MÉTODO: busca no banco e popula os 2 combos
        FuncionarioRepositorio repositorio = new FuncionarioRepositorio();
        try {
            List<Funcionario> guias = repositorio.listarPorPapel("GUIA");
            comboGuia.setItems(FXCollections.observableArrayList(guias));
            comboGuia.setConverter(criarConversorFuncionario());

            List<Funcionario> recepcionistas = repositorio.listarPorPapel("RECEPCIONISTA");
            Funcionario nenhuma = new Funcionario(); // representa a opção "Nenhuma"
            nenhuma.setNome("Nenhuma");
            recepcionistas.add(0, nenhuma); // adiciona "Nenhuma" como primeiro item da lista

            comboRecepcionista.setItems(FXCollections.observableArrayList(recepcionistas));
            comboRecepcionista.setConverter(criarConversorFuncionario());
            comboRecepcionista.getSelectionModel().selectFirst(); // já inicia com "Nenhuma" selecionada
        } catch (SQLException e) {
            System.out.println("Erro ao carregar funcionários: " + e.getMessage());
        }
    }

    private void carregarValores(){
        ConfiguracaoRepositorio repositorio = new ConfiguracaoRepositorio();

        try {
            Configuracao configuracao = repositorio.buscar();
            if (configuracao != null) {
                valorInteira = configuracao.getValorInteira();
                valorMeia = configuracao.getValorMeia();
            } else {
                valorInteira = 30.00;
                valorMeia = 15.00;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar valores: " + e.getMessage());
            valorInteira = 30.00;
            valorMeia = 15.00;
        }
        atualizarTela();
    }

    private void configurarCatraca() { // MÉTODO: busca os dados de conexão no banco e abastece o ServicoControlId
        ConfiguracaoRepositorio repositorio = new ConfiguracaoRepositorio();
        try {
            Configuracao configuracao = repositorio.buscar();
            if (configuracao != null && configuracao.getIpCatraca() != null) {
                servicoControlId.configurar(
                        configuracao.getIpCatraca(),
                        configuracao.getPortaCatraca(),
                        configuracao.getUsuarioCatraca(),
                        configuracao.getSenhaCatraca()
                );
            } else {
                System.out.println("Catraca não configurada. Vá em Configuração para definir IP/porta/usuário/senha.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar configuração da catraca: " + e.getMessage());
        }
    }

    @FXML
    public void liberarEntrada() { // MÉTODO: botão "Liberar Entrada" (sentido horário)
        liberar(SentidoCatraca.HORARIO, "entrada");
    }

    @FXML
    public void liberarSaida() { // MÉTODO: botão "Liberar Saída" (sentido anti-horário)
        liberar(SentidoCatraca.ANTI_HORARIO, "saída");
    }

    private void liberar(SentidoCatraca sentido, String descricao) { // MÉTODO privado: lógica comum aos dois botões
        try {
            servicoControlId.liberarCatraca(sentido);
            System.out.println("Catraca liberada para " + descricao + ".");
        } catch (Exception e) {
            System.out.println("Erro ao liberar " + descricao + ": " + e.getMessage());
        }
    }

    private StringConverter<Funcionario> criarConversorFuncionario() { // MÉTODO: reaproveitado pelos 2 combos
        return new StringConverter<>() {
            @Override
            public String toString(Funcionario funcionario) {
                return funcionario == null ? "" : funcionario.getNome();
            }

            @Override
            public Funcionario fromString(String texto) {
                return null;
            }
        };
    }

    @FXML public void incrementarInteira() { quantidadeInteira++; atualizarTela(); }
    @FXML public void decrementarInteira() { if (quantidadeInteira > 0) quantidadeInteira--; atualizarTela(); }
    @FXML public void incrementarMeia() { quantidadeMeia++; atualizarTela(); }
    @FXML public void decrementarMeia() { if (quantidadeMeia > 0) quantidadeMeia--; atualizarTela(); }
    @FXML public void incrementarNaoPagante() { quantidadeNaoPagante++; atualizarTela(); }
    @FXML public void decrementarNaoPagante() { if (quantidadeNaoPagante > 0) quantidadeNaoPagante--; atualizarTela(); }

    private void atualizarTela() { // MÉTODO: sincroniza os Labels da tela com o estado atual dos contadores
        labelInteira.setText(String.valueOf(quantidadeInteira));
        labelMeia.setText(String.valueOf(quantidadeMeia));
        labelNaoPagante.setText(String.valueOf(quantidadeNaoPagante));

        double total = (quantidadeInteira * valorInteira) + (quantidadeMeia * valorMeia);
        labelValorTotal.setText("Valor Total: R$ " + String.format("%.2f", total));
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Aviso");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    @FXML
    public void iniciarVisita() {
        Funcionario guia = comboGuia.getValue();
        Funcionario recepcionista = comboRecepcionista.getValue();

        if (guia == null) { // validação: guia é obrigatório
            System.out.println("Selecione uma guia para iniciar a visita.");
            return;
        }

        if (quantidadeInteira == 0 && quantidadeMeia == 0 && quantidadeNaoPagante == 0) { // validação: precisa de pelo menos 1 pessoa
            System.out.println("Adicione pelo menos uma pessoa à visita.");
            return;
        }
        double valorTotal = (quantidadeInteira * valorInteira) + (quantidadeMeia * valorMeia);

        double dinheiro = lerValorCampo(campoDinheiro);
        double pix = lerValorCampo(campoPix);
        double debito = lerValorCampo(campoDebito);

        if (dinheiro < 0 || pix < 0 || debito < 0) { // nenhum valor pode ser negativo
            mostrarAviso("Os valores de pagamento não podem ser negativos.");
            return;
        }

        double somaPagamento = dinheiro + pix + debito;

        if (Math.abs(somaPagamento - valorTotal) > 0.001) { // a soma tem que bater com o total
            mostrarAviso("A soma das formas de pagamento (R$ " + String.format("%.2f", somaPagamento) +
                    ") não bate com o valor total (R$ " + String.format("%.2f", valorTotal) + ").");
            return;
        }

        Visita visita = new Visita();

        visita.setGuiaId(guia.getId());
        visita.setGuiaTipoRemuneracao(guia.getTipoRemuneracao());
        visita.setGuiaValorRemuneracao(guia.getValorRemuneracao());


        if (recepcionista != null && recepcionista.getId() != null) { // se tem recepcionista de verdade (não é "Nenhuma")
            visita.setRecepcionistaId(recepcionista.getId());
            visita.setRecepcionistaTipoRemuneracao(recepcionista.getTipoRemuneracao());
            visita.setRecepcionistaValorRemuneracao(recepcionista.getValorRemuneracao());
        }
        // se for "Nenhuma", não setamos nada -- os campos ficam null por padrão, como decidimos

        visita.setQuantidadeInteira(quantidadeInteira);
        visita.setQuantidadeMeia(quantidadeMeia);
        visita.setQuantidadeNaoPagante(quantidadeNaoPagante);

        visita.setValorUnitarioInteira(valorInteira);
        visita.setValorUnitarioMeia(valorMeia);
        visita.setValorTotal((quantidadeInteira * valorInteira) + (quantidadeMeia * valorMeia));

        visita.setObservacoes(campoObservacoes.getText());
        visita.setDataHoraInicio(LocalDateTime.now().toString());
        visita.setStatus("ATIVA");
        visita.setValorDinheiro(dinheiro);
        visita.setValorPix(pix);
        visita.setValorDebito(debito);

        VisitaRepositorio repositorio = new VisitaRepositorio();
        try {
            repositorio.salvar(visita);
            System.out.println("Visita iniciada e salva! ID: " + visita.getId());
            limparFormulario();
            carregarUltimaVisita();
        } catch (SQLException e) {
            System.out.println("Erro ao salvar visita: " + e.getMessage());
        }
    }


    private void limparFormulario() { // MÉTODO: zera a tela após salvar uma visita
        quantidadeInteira = 0;
        quantidadeMeia = 0;
        quantidadeNaoPagante = 0;
        comboGuia.getSelectionModel().clearSelection();
        comboRecepcionista.getSelectionModel().selectFirst(); // volta pra "Nenhuma"
        campoObservacoes.clear();
        campoDinheiro.setText("0");
        campoPix.setText("0");
        campoDebito.setText("0");
        atualizarTela();
    }

    private void configurarSomaPagamento() { // MÉTODO: atualiza a soma dos pagamentos sempre que um campo muda
        campoDinheiro.textProperty().addListener((obs, antigo, novo) -> atualizarSomaPagamento());
        campoPix.textProperty().addListener((obs, antigo, novo) -> atualizarSomaPagamento());
        campoDebito.textProperty().addListener((obs, antigo, novo) -> atualizarSomaPagamento());
    }

    private void atualizarSomaPagamento() { // MÉTODO: lê os 3 campos, soma, e mostra no label
        double soma = lerValorCampo(campoDinheiro) + lerValorCampo(campoPix) + lerValorCampo(campoDebito);
        labelSomaPagamento.setText("Soma: R$ " + String.format("%.2f", soma));
    }

    private double lerValorCampo(TextField campo) { // MÉTODO auxiliar: lê um campo como número, tratando vazio/inválido
        try {
            String texto = campo.getText().replace(",", ".").trim();
            return texto.isBlank() ? 0.0 : Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return 0.0; // se digitou algo inválido, considera 0 (a validação final barra depois)
        }
    }
}