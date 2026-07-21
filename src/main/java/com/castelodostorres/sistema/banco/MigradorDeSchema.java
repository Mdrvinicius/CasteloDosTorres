package com.castelodostorres.sistema.banco;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class MigradorDeSchema {

    public static void migrar() throws SQLException { // MÉTODO: ponto de entrada, chamado pelo Inicializador
        Connection conexao = GerenciadorConexao.getConexao();

        criarTabelaDeControle(conexao); // garante que schema_migrations existe, antes de qualquer coisa
        int versaoAtual = buscarVersaoAtual(conexao); // descobre até onde esse banco específico já foi atualizado

        System.out.println("VERSAO ATUAL DO BANCO: " + versaoAtual);
        aplicarSeNecessario(conexao, versaoAtual, 1, MigradorDeSchema::migracaoVersao1);
        aplicarSeNecessario(conexao, versaoAtual, 2, MigradorDeSchema::migracaoVersao2);
        aplicarSeNecessario(conexao, versaoAtual, 3, MigradorDeSchema::migracaoVersao3);
        aplicarSeNecessario(conexao, versaoAtual, 4, MigradorDeSchema::migracaoVersao4);
        aplicarSeNecessario(conexao, versaoAtual, 5, MigradorDeSchema::migracaoVersao5);
        aplicarSeNecessario(conexao, versaoAtual, 6, MigradorDeSchema::migracaoVersao6);
        // no futuro, cada mudança nova de schema vira mais uma linha aqui, com número seguinte (3, 4, 5...)
    }

    private static void criarTabelaDeControle(Connection conexao) throws SQLException {
        try (Statement comando = conexao.createStatement()) {
            comando.execute("""
                CREATE TABLE IF NOT EXISTS schema_migrations (
                    versao INTEGER PRIMARY KEY,
                    aplicada_em TEXT NOT NULL
                )
                """);
        }
    }

    private static int buscarVersaoAtual(Connection conexao) throws SQLException {
        try (Statement comando = conexao.createStatement();
             ResultSet resultado = comando.executeQuery("SELECT MAX(versao) AS maior FROM schema_migrations")) {

            if (resultado.next()) {
                int maior = resultado.getInt("maior");
                return resultado.wasNull() ? 0 : maior; // se a tabela está vazia (banco novinho), considera versão 0
            }
            return 0;
        }
    }

    private static void aplicarSeNecessario(Connection conexao, int versaoAtual, int versaoDaMigracao, Migracao migracao) throws SQLException {
        if (versaoDaMigracao > versaoAtual) { // só roda se essa versão específica ainda não foi aplicada nesse banco
            migracao.executar(conexao);
            registrarVersaoAplicada(conexao, versaoDaMigracao);
            System.out.println("Migração versão " + versaoDaMigracao + " aplicada com sucesso.");
        }
    }

    private static void registrarVersaoAplicada(Connection conexao, int versao) throws SQLException {
        try (var comando = conexao.prepareStatement("INSERT INTO schema_migrations (versao, aplicada_em) VALUES (?, ?)")) {
            comando.setInt(1, versao);
            comando.setString(2, LocalDateTime.now().toString());
            comando.executeUpdate();
        }
    }

    @FunctionalInterface
    private interface Migracao { // representa "uma migração", só uma promessa de método que executa SQL
        void executar(Connection conexao) throws SQLException;
    }

    private static void migracaoVersao1(Connection conexao) throws SQLException { // a fundação: as 4 tabelas originais
        try (Statement comando = conexao.createStatement()) {
            comando.execute("""
                CREATE TABLE IF NOT EXISTS funcionario (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    papel TEXT NOT NULL CHECK (papel IN ('GUIA', 'RECEPCIONISTA')),
                    tipo_remuneracao TEXT NOT NULL CHECK (tipo_remuneracao IN ('PERCENTUAL', 'FIXO_POR_PESSOA')),
                    valor_remuneracao REAL NOT NULL
                )
                """);

            comando.execute("""
                CREATE TABLE IF NOT EXISTS configuracao (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    ip_catraca TEXT,
                    porta_catraca INTEGER,
                    usuario_catraca TEXT,
                    senha_catraca TEXT,
                    valor_inteira REAL NOT NULL DEFAULT 30.00,
                    valor_meia REAL NOT NULL DEFAULT 15.00
                )
                """);

            comando.execute("""
                CREATE TABLE IF NOT EXISTS visita (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    guia_id INTEGER NOT NULL REFERENCES funcionario(id),
                    recepcionista_id INTEGER REFERENCES funcionario(id),
                    guia_tipo_remuneracao TEXT NOT NULL CHECK (guia_tipo_remuneracao IN ('PERCENTUAL', 'FIXO_POR_PESSOA')),
                    guia_valor_remuneracao REAL NOT NULL,
                    recepcionista_tipo_remuneracao TEXT CHECK (recepcionista_tipo_remuneracao IN ('PERCENTUAL', 'FIXO_POR_PESSOA')),
                    recepcionista_valor_remuneracao REAL,
                    quantidade_inteira INTEGER NOT NULL DEFAULT 0,
                    quantidade_meia INTEGER NOT NULL DEFAULT 0,
                    quantidade_nao_pagante INTEGER NOT NULL DEFAULT 0,
                    valor_unitario_inteira REAL NOT NULL,
                    valor_unitario_meia REAL NOT NULL,
                    valor_total REAL NOT NULL,
                    observacoes TEXT,
                    data_hora_inicio TEXT NOT NULL,
                    status TEXT NOT NULL CHECK (status IN ('ATIVA', 'CANCELADA')) DEFAULT 'ATIVA',
                    motivo_cancelamento TEXT,
                    data_hora_cancelamento TEXT
                )
                """);

            comando.execute("""
                CREATE TABLE IF NOT EXISTS evento_liberacao (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    visita_id INTEGER REFERENCES visita(id),
                    data_hora TEXT NOT NULL,
                    sentido TEXT NOT NULL CHECK (sentido IN ('HORARIO', 'ANTI_HORARIO')),
                    sucesso INTEGER NOT NULL CHECK (sucesso IN (0, 1)),
                    detalhe TEXT
                )
                """);
        }
    }

    private static void migracaoVersao2(Connection conexao) throws SQLException { // adiciona a senha de administrador
        try (Statement comando = conexao.createStatement()) {
            comando.execute("ALTER TABLE configuracao ADD COLUMN senha_admin TEXT");
        }
    }

    private static void migracaoVersao3(Connection conexao) throws SQLException { // adiciona reembolso e forma de pagamento
        try (Statement comando = conexao.createStatement()) {
            comando.execute("ALTER TABLE visita ADD COLUMN valor_reembolsado REAL NOT NULL DEFAULT 0");
            comando.execute("ALTER TABLE visita ADD COLUMN motivo_reembolso TEXT");
            comando.execute("ALTER TABLE visita ADD COLUMN valor_dinheiro REAL NOT NULL DEFAULT 0");
            comando.execute("ALTER TABLE visita ADD COLUMN valor_pix REAL NOT NULL DEFAULT 0");
            comando.execute("ALTER TABLE visita ADD COLUMN valor_debito REAL NOT NULL DEFAULT 0");

        }
    }

    private static void migracaoVersao4(Connection conexao) throws SQLException { // cria a tabela de fundo de troco
        try (Statement comando = conexao.createStatement()) {
            comando.execute("""
            CREATE TABLE IF NOT EXISTS fundo_troco (
                data TEXT PRIMARY KEY,
                valor REAL NOT NULL
            )
            """);
        }
    }

    private static void migracaoVersao5(Connection conexao) throws SQLException { // soft delete de funcionário
        try (Statement comando = conexao.createStatement()) {
            comando.execute("ALTER TABLE funcionario ADD COLUMN ativo INTEGER NOT NULL DEFAULT 1");
        }
    }

    private static void migracaoVersao6(Connection conexao) throws SQLException { // cria tabelas de despesas
        try (Statement comando = conexao.createStatement()) {
            comando.execute("""
            CREATE TABLE IF NOT EXISTS despesa (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                valor REAL NOT NULL,
                tipo TEXT NOT NULL CHECK (tipo IN ('AVULSA', 'RECORRENTE')),
                data_hora_cadastro TEXT NOT NULL
            )
            """);

            comando.execute("""
            CREATE TABLE IF NOT EXISTS despesa_mensal (
                despesa_id INTEGER NOT NULL REFERENCES despesa(id),
                mes TEXT NOT NULL,
                valor REAL NOT NULL,
                PRIMARY KEY (despesa_id, mes)
            )
            """);
        }
    }
}