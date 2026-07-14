package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.Visita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class VisitaRepositorio {

    public void salvar(Visita visita) throws SQLException { // MÉTODO: insere uma nova visita no banco
        String sql = """
            INSERT INTO visita (
                guia_id, recepcionista_id,
                guia_tipo_remuneracao, guia_valor_remuneracao,
                recepcionista_tipo_remuneracao, recepcionista_valor_remuneracao,
                quantidade_inteira, quantidade_meia, quantidade_nao_pagante,
                valor_unitario_inteira, valor_unitario_meia, valor_total,
                observacoes, data_hora_inicio, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setInt(1, visita.getGuiaId());

            if (visita.getRecepcionistaId() == null) {
                comando.setNull(2, Types.INTEGER);
            } else {
                comando.setInt(2, visita.getRecepcionistaId());
            }

            comando.setString(3, visita.getGuiaTipoRemuneracao());
            comando.setDouble(4, visita.getGuiaValorRemuneracao());

            if (visita.getRecepcionistaTipoRemuneracao() == null) {
                comando.setNull(5, Types.VARCHAR);
                comando.setNull(6, Types.REAL);
            } else {
                comando.setString(5, visita.getRecepcionistaTipoRemuneracao());
                comando.setDouble(6, visita.getRecepcionistaValorRemuneracao());
            }

            comando.setInt(7, visita.getQuantidadeInteira());
            comando.setInt(8, visita.getQuantidadeMeia());
            comando.setInt(9, visita.getQuantidadeNaoPagante());
            comando.setDouble(10, visita.getValorUnitarioInteira());
            comando.setDouble(11, visita.getValorUnitarioMeia());
            comando.setDouble(12, visita.getValorTotal());
            comando.setString(13, visita.getObservacoes());
            comando.setString(14, visita.getDataHoraInicio());
            comando.setString(15, visita.getStatus());

            comando.executeUpdate();

            try (ResultSet chavesGeradas = comando.getGeneratedKeys()) {
                if (chavesGeradas.next()) {
                    visita.setId(chavesGeradas.getInt(1));
                }
            }
        }
    }
}