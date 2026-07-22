package com.castelodostorres.sistema.controlador;

import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.modelo.dto.DadosReembolso;
import com.castelodostorres.sistema.repositorio.VisitaRepositorio;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Optional;

public class ControladorTelaDetalhesVisita implements PrecisaDaTelaRaiz{

    @FXML private Label labelGuia;
    @FXML private Label labelRecepcionista;
    @FXML private Label labelDataHora;
    @FXML private Label labelInteiras;
    @FXML private Label labelMeias;
    @FXML private Label labelNaoPagantes;
    @FXML private Label labelValorTotal;
    @FXML private Label labelReembolso;
    @FXML private Label labelValorLiquido;
    @FXML private Label labelStatus;
    @FXML private Label labelObservacoes;
    @FXML private Label labelMotivoCancelamento;
    @FXML private Label labelMotivoReembolso;
    @FXML private javafx.scene.layout.VBox caixaFormasPagamento;

    private Visita visita; // ATRIBUTO: a visita que esta tela está mostrando

    private ControladorTelaRaiz telaRaiz;

    @Override
    public void setTelaRaiz(ControladorTelaRaiz telaRaiz) {
        this.telaRaiz = telaRaiz;
    }

    public void setVisita(Visita visita) { // MÉTODO: recebe a visita de quem abriu esta tela, e preenche os labels
        this.visita = visita;
        preencherCampos();
    }

    private void preencherCampos() { // MÉTODO: joga os dados da visita nos labels da tela
        labelGuia.setText(visita.getNomeGuia());
        labelRecepcionista.setText(visita.getNomeRecepcionista() == null ? "Nenhuma" : visita.getNomeRecepcionista());
        labelDataHora.setText(com.castelodostorres.sistema.util.FormatadorData.formatar(visita.getDataHoraInicio()));
        labelInteiras.setText(String.valueOf(visita.getQuantidadeInteira()));
        labelMeias.setText(String.valueOf(visita.getQuantidadeMeia()));
        labelNaoPagantes.setText(String.valueOf(visita.getQuantidadeNaoPagante()));
        labelValorTotal.setText("R$ " + String.format("%.2f", visita.getValorTotal()));
        labelReembolso.setText("R$ " + String.format("%.2f", visita.getValorReembolsado()));


        double liquido = visita.getValorTotal() - visita.getValorReembolsado();
        labelValorLiquido.setText("R$ " + String.format("%.2f", liquido));

        labelStatus.setText(visita.getStatus());
        // cor do status: verde pra ATIVA, laranja pra CANCELADA
        labelStatus.getStyleClass().removeAll("badge-ativa", "badge-cancelada");
        if ("CANCELADA".equals(visita.getStatus())) {
            labelStatus.getStyleClass().add("badge-cancelada");
        } else {
            labelStatus.getStyleClass().add("badge-ativa");
        }

        labelObservacoes.setText(visita.getObservacoes() == null || visita.getObservacoes().isBlank() ? "-" : visita.getObservacoes());

        // Motivo do cancelamento: só aparece se a visita foi cancelada
        if ("CANCELADA".equals(visita.getStatus()) && visita.getMotivoCancelamento() != null) {
            labelMotivoCancelamento.setText("Motivo do cancelamento: " + visita.getMotivoCancelamento());
            labelMotivoCancelamento.setVisible(true);
            labelMotivoCancelamento.setManaged(true);
        } else {
            labelMotivoCancelamento.setVisible(false);
            labelMotivoCancelamento.setManaged(false);
        }

        // Motivo do reembolso: só aparece se houve reembolso
        if (visita.getValorReembolsado() > 0 && visita.getMotivoReembolso() != null) {
            labelMotivoReembolso.setText("Motivo do reembolso: " + visita.getMotivoReembolso());
            labelMotivoReembolso.setVisible(true);
            labelMotivoReembolso.setManaged(true);
        } else {
            labelMotivoReembolso.setVisible(false);
            labelMotivoReembolso.setManaged(false);
        }
        caixaFormasPagamento.getChildren().clear();
        adicionarFormaSePresente("Dinheiro", visita.getDinheiroBruto());
        adicionarFormaSePresente("Pix", visita.getPixBruto());
        adicionarFormaSePresente("Débito", visita.getDebitoBruto());
        if (caixaFormasPagamento.getChildren().isEmpty()) {
            Label vazio = new Label("-");
            vazio.getStyleClass().add("detalhe-valor");
            caixaFormasPagamento.getChildren().add(vazio);
        }

    }

    @FXML
    public void voltar() { // agora volta de verdade pra lista
        if (telaRaiz != null) {
            telaRaiz.abrirVisitas();
        }
    }
    @FXML
    public void cancelarVisita() { // MÉTODO: pede motivo e cancela a visita
        if ("CANCELADA".equals(visita.getStatus())) { // proteção: já está cancelada
            mostrarAviso("Esta visita já está cancelada.");
            return;
        }

        TextInputDialog dialogo = new TextInputDialog(); // cria o diálogo de entrada de texto
        dialogo.setTitle("Cancelar Visita");
        dialogo.setHeaderText("Informe o motivo do cancelamento:");
        dialogo.setContentText("Motivo:");

        Optional<String> resultado = dialogo.showAndWait(); // mostra e espera o usuário responder

        if (resultado.isEmpty()) { // usuário clicou em Cancelar/fechou o diálogo
            return;
        }

        String motivo = resultado.get().trim();
        if (motivo.isBlank()) { // motivo é obrigatório
            mostrarAviso("O motivo do cancelamento é obrigatório.");
            return;
        }

        try {
            VisitaRepositorio repositorio = new VisitaRepositorio();
            repositorio.cancelar(visita.getId(), motivo);

            visita.setStatus("CANCELADA"); // atualiza o objeto em memória também
            preencherCampos();              // atualiza a tela pra refletir o novo status
            mostrarAviso("Visita cancelada com sucesso.");
        } catch (SQLException e) {
            mostrarAviso("Erro ao cancelar: " + e.getMessage());
        }
    }

    private void mostrarAviso(String mensagem) { // MÉTODO auxiliar: mostra uma mensagem simples ao usuário
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Aviso");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
    @FXML
    public void reembolsar() { // MÉTODO: abre diálogo customizado com valor + motivo, e registra o reembolso
        if ("CANCELADA".equals(visita.getStatus())) { // não faz sentido reembolsar visita cancelada
            mostrarAviso("Não é possível reembolsar uma visita cancelada.");
            return;
        }

        Dialog<DadosReembolso> dialogo = new Dialog<>();
        dialogo.setTitle("Reembolsar Valor");
        dialogo.setHeaderText("Informe o valor e o motivo do reembolso:");

        ButtonType botaoConfirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(botaoConfirmar, ButtonType.CANCEL);

        TextField campoValor = new TextField();
        campoValor.setPromptText("Ex: 15,00");
        TextArea campoMotivo = new TextArea();
        campoMotivo.setPromptText("Motivo do reembolso");
        campoMotivo.setPrefRowCount(3);

        ComboBox<String> comboForma = new ComboBox<>();
        comboForma.getItems().addAll("DINHEIRO", "PIX");
        comboForma.getSelectionModel().selectFirst(); // já começa com DINHEIRO selecionado

        VBox conteudo = new VBox(10,
                new Label("Valor a reembolsar (R$):"), campoValor,
                new Label("Reembolso em:"), comboForma,
                new Label("Motivo:"), campoMotivo
        );

        dialogo.getDialogPane().setContent(conteudo);

        dialogo.setResultConverter(botaoClicado -> {
            if (botaoClicado == botaoConfirmar) {
                try {
                    double valor = Double.parseDouble(campoValor.getText().replace(",", "."));
                    return new DadosReembolso(valor, campoMotivo.getText().trim(), comboForma.getValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<DadosReembolso> resultado = dialogo.showAndWait();

        if (resultado.isEmpty()) { // cancelou ou digitou valor inválido
            return;
        }

        DadosReembolso dados = resultado.get();

        if (dados.getValor() <= 0) { // valor precisa ser positivo
            mostrarAviso("O valor do reembolso deve ser maior que zero.");
            return;
        }
        double jaReembolsado = visita.getValorReembolsado();
        double disponivelParaReembolso = visita.getValorTotal() - jaReembolsado;

        if (dados.getValor() > disponivelParaReembolso) {
            mostrarAviso("O reembolso não pode ultrapassar o valor disponível (R$ " + String.format("%.2f", disponivelParaReembolso) + ").");
            return;
        }
        if (dados.getMotivo().isBlank()) { // motivo obrigatório
            mostrarAviso("O motivo do reembolso é obrigatório.");
            return;
        }

        try {
            VisitaRepositorio repositorio = new VisitaRepositorio();
            repositorio.reembolsar(visita.getId(), dados.getValor(), dados.getMotivo(), dados.getForma());

            visita.setValorReembolsado(visita.getValorReembolsado() + dados.getValor()); // soma, não substitui

            if ("DINHEIRO".equals(dados.getForma())) {
                visita.setValorDinheiro(visita.getValorDinheiro() - dados.getValor());
            } else {
                visita.setValorPix(visita.getValorPix() - dados.getValor());
            }

            visita.setMotivoReembolso(dados.getMotivo());
            preencherCampos();                             // atualiza a tela (o valor líquido recalcula)
            mostrarAviso("Reembolso registrado com sucesso.");
        } catch (SQLException e) {
            mostrarAviso("Erro ao registrar reembolso: " + e.getMessage());
        }
    }
    private void adicionarFormaSePresente(String nome, double valor) { // MÉTODO: cria uma linha da forma só se valor > 0
        if (valor > 0) {
            Label linha = new Label(nome + ": R$ " + String.format("%.2f", valor));
            linha.getStyleClass().add("detalhe-valor");
            caixaFormasPagamento.getChildren().add(linha);
        }
    }
}