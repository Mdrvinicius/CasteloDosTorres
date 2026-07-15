package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.Visita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public List<Visita> listarTodas() throws SQLException { // MÉTODO: traz todas as visitas, com o nome da guia junto
        String sql = """
        SELECT v.*, f.nome AS nome_guia, r.nome AS nome_recepcionista
        FROM visita v
        JOIN funcionario f ON v.guia_id = f.id
        LEFT JOIN funcionario r ON v.recepcionista_id = r.id
        ORDER BY v.data_hora_inicio DESC
        """;

        List<Visita> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                Visita visita = new Visita();
                visita.setId(resultado.getInt("id"));
                visita.setGuiaId(resultado.getInt("guia_id"));
                visita.setNomeGuia(resultado.getString("nome_guia")); // campo novo, explico abaixo
                visita.setNomeRecepcionista(resultado.getString("nome_recepcionista"));
                visita.setQuantidadeInteira(resultado.getInt("quantidade_inteira"));
                visita.setQuantidadeMeia(resultado.getInt("quantidade_meia"));
                visita.setQuantidadeNaoPagante(resultado.getInt("quantidade_nao_pagante"));
                visita.setValorTotal(resultado.getDouble("valor_total"));
                visita.setValorReembolsado(resultado.getDouble("valor_reembolsado"));
                visita.setDataHoraInicio(resultado.getString("data_hora_inicio"));
                visita.setStatus(resultado.getString("status"));
                lista.add(visita);
            }
        }

        return lista;
    }

    public void cancelar(int visitaId, String motivo) throws SQLException { // MÉTODO: marca uma visita como cancelada (soft delete)
        String sql = """
        UPDATE visita
        SET status = 'CANCELADA',
            motivo_cancelamento = ?,
            data_hora_cancelamento = ?
        WHERE id = ?
        """;

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, motivo);
            comando.setString(2, LocalDateTime.now().toString());
            comando.setInt(3, visitaId);
            comando.executeUpdate();
        }
    }

    public void reembolsar(int visitaId, double valorReembolso, String motivo) throws SQLException { // MÉTODO: registra reembolso
        String sql = """
        UPDATE visita
        SET valor_reembolsado = valor_reembolsado + ?,
            motivo_reembolso = CASE
                WHEN motivo_reembolso IS NULL OR motivo_reembolso = '' THEN ?
                ELSE motivo_reembolso || ' | ' || ?
            END
        WHERE id = ?
        """;

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setDouble(1, valorReembolso);
            comando.setString(2, motivo); // usado quando ainda não há motivo anterior
            comando.setString(3, motivo); // usado quando concatena ao motivo anterior
            comando.setInt(4, visitaId);
            comando.executeUpdate();
        }
    }
}