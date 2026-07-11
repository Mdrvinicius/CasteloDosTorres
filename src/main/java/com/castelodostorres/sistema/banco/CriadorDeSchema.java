package com.castelodostorres.sistema.banco;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CriadorDeSchema {

    public static void criarTabelas() throws SQLException {

        Connection conexao = GerenciadorConexao.getConexao();

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
}
