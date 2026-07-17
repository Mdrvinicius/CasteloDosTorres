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

    public Visita buscarUltima() throws SQLException { // MÉTODO: retorna a visita mais recente (ou null se não houver)
        String sql = """
        SELECT v.*, f.nome AS nome_guia, r.nome AS nome_recepcionista
        FROM visita v
        JOIN funcionario f ON v.guia_id = f.id
        LEFT JOIN funcionario r ON v.recepcionista_id = r.id
        ORDER BY v.data_hora_inicio DESC
        LIMIT 1
        """;

        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            if (resultado.next()) {
                Visita visita = new Visita();
                visita.setId(resultado.getInt("id"));
                visita.setNomeGuia(resultado.getString("nome_guia"));
                visita.setNomeRecepcionista(resultado.getString("nome_recepcionista"));
                visita.setQuantidadeInteira(resultado.getInt("quantidade_inteira"));
                visita.setQuantidadeMeia(resultado.getInt("quantidade_meia"));
                visita.setQuantidadeNaoPagante(resultado.getInt("quantidade_nao_pagante"));
                visita.setValorTotal(resultado.getDouble("valor_total"));
                visita.setValorReembolsado(resultado.getDouble("valor_reembolsado"));
                visita.setDataHoraInicio(resultado.getString("data_hora_inicio"));
                visita.setStatus(resultado.getString("status"));
                visita.setObservacoes(resultado.getString("observacoes"));
                return visita;
            }
        }
        return null; // nenhuma visita ainda
    }

    public void salvar(Visita visita) throws SQLException { // MÉTODO: insere uma nova visita no banco
        String sql = """
            INSERT INTO visita (
                guia_id, recepcionista_id,
                guia_tipo_remuneracao, guia_valor_remuneracao,
                recepcionista_tipo_remuneracao, recepcionista_valor_remuneracao,
                quantidade_inteira, quantidade_meia, quantidade_nao_pagante,
                valor_unitario_inteira, valor_unitario_meia, valor_total,
                observacoes, data_hora_inicio, status,
                valor_dinheiro, valor_pix, valor_debito
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            comando.setDouble(16, visita.getValorDinheiro());
            comando.setDouble(17, visita.getValorPix());
            comando.setDouble(18, visita.getValorDebito());

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
                visita.setMotivoCancelamento(resultado.getString("motivo_cancelamento"));
                visita.setMotivoReembolso(resultado.getString("motivo_reembolso"));
                visita.setObservacoes(resultado.getString("observacoes"));
                visita.setStatus(resultado.getString("status"));
                visita.setValorDinheiro(resultado.getDouble("valor_dinheiro"));
                visita.setValorPix(resultado.getDouble("valor_pix"));
                visita.setValorDebito(resultado.getDouble("valor_debito"));
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

    public void reembolsar(int visitaId, double valorReembolso, String motivo, String forma) throws SQLException {
        String colunaForma = "DINHEIRO".equals(forma) ? "valor_dinheiro" : "valor_pix";

        String sql = """
        UPDATE visita
        SET valor_reembolsado = valor_reembolsado + ?,
            motivo_reembolso = CASE
                WHEN motivo_reembolso IS NULL OR motivo_reembolso = '' THEN ?
                ELSE motivo_reembolso || ' | ' || ?
            END,
            %s = %s - ?
        WHERE id = ?
        """.formatted(colunaForma, colunaForma);

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setDouble(1, valorReembolso);
            comando.setString(2, motivo);
            comando.setString(3, motivo);
            comando.setDouble(4, valorReembolso);
            comando.setInt(5, visitaId);
            comando.executeUpdate();
        }
    }

    public double calcularTotalArrecadadoDoDia(String data) throws SQLException { // MÉTODO: soma o valor líquido das visitas de um dia
        String sql = """
        SELECT COALESCE(SUM(valor_total - valor_reembolsado), 0) AS total
        FROM visita
        WHERE date(data_hora_inicio) = ?
          AND status != 'CANCELADA'
        """;

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, data);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getDouble("total");
                }
            }
        }

        return 0.0;
    }

    public double[] calcularFormasPagamentoDoDia(String data) throws SQLException { // MÉTODO: soma dinheiro, pix e débito do dia
        String sql = """
        SELECT
            COALESCE(SUM(valor_dinheiro), 0) AS dinheiro,
            COALESCE(SUM(valor_pix), 0) AS pix,
            COALESCE(SUM(valor_debito), 0) AS debito
        FROM visita
        WHERE date(data_hora_inicio) = ?
          AND status != 'CANCELADA'
        """;

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, data);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    double dinheiro = resultado.getDouble("dinheiro");
                    double pix = resultado.getDouble("pix");
                    double debito = resultado.getDouble("debito");
                    return new double[] { dinheiro, pix, debito };
                }
            }
        }

        return new double[] { 0, 0, 0 };
    }

    public List<Visita> listarDoDia(String data) throws SQLException { // MÉTODO: visitas não-canceladas de um dia, com nomes
        String sql = """
        SELECT v.*, f.nome AS nome_guia, r.nome AS nome_recepcionista
        FROM visita v
        JOIN funcionario f ON v.guia_id = f.id
        LEFT JOIN funcionario r ON v.recepcionista_id = r.id
        WHERE date(v.data_hora_inicio) = ?
          AND v.status != 'CANCELADA'
        """;

        List<Visita> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, data);

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    Visita visita = new Visita();
                    visita.setId(resultado.getInt("id"));
                    visita.setNomeGuia(resultado.getString("nome_guia"));
                    visita.setNomeRecepcionista(resultado.getString("nome_recepcionista"));
                    visita.setQuantidadeInteira(resultado.getInt("quantidade_inteira"));
                    visita.setQuantidadeMeia(resultado.getInt("quantidade_meia"));
                    visita.setValorTotal(resultado.getDouble("valor_total"));
                    visita.setValorReembolsado(resultado.getDouble("valor_reembolsado"));
                    visita.setGuiaTipoRemuneracao(resultado.getString("guia_tipo_remuneracao"));
                    visita.setGuiaValorRemuneracao(resultado.getDouble("guia_valor_remuneracao"));
                    visita.setRecepcionistaTipoRemuneracao(resultado.getString("recepcionista_tipo_remuneracao"));
                    Object recepValor = resultado.getObject("recepcionista_valor_remuneracao");
                    visita.setRecepcionistaValorRemuneracao(recepValor == null ? null : ((Number) recepValor).doubleValue());
                    lista.add(visita);
                }
            }
        }

        return lista;
    }

    public double[] calcularEstatisticasDoDia(String data) throws SQLException { // MÉTODO: contagens e totais do dia numa consulta só
        String sql = """
        SELECT
            COUNT(*) AS qtd_visitas,
            COALESCE(SUM(quantidade_inteira), 0) AS total_inteiras,
            COALESCE(SUM(quantidade_meia), 0) AS total_meias,
            COALESCE(SUM(quantidade_nao_pagante), 0) AS total_nao_pagantes,
            COALESCE(SUM(valor_total), 0) AS total_bruto,
            COALESCE(SUM(valor_reembolsado), 0) AS total_reembolsos
        FROM visita
        WHERE date(data_hora_inicio) = ?
          AND status != 'CANCELADA'
        """;

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, data);

            try (ResultSet r = comando.executeQuery()) {
                if (r.next()) {
                    return new double[] {
                            r.getInt("qtd_visitas"),
                            r.getInt("total_inteiras"),
                            r.getInt("total_meias"),
                            r.getInt("total_nao_pagantes"),
                            r.getDouble("total_bruto"),
                            r.getDouble("total_reembolsos")
                    };
                }
            }
        }

        return new double[] { 0, 0, 0, 0, 0, 0 };
    }
    public double calcularTotalArrecadadoDoMes(String mes) throws SQLException { // MÉTODO: total líquido do mês (mes = "aaaa-mm")
        String sql = """
        SELECT COALESCE(SUM(valor_total - valor_reembolsado), 0) AS total
        FROM visita
        WHERE strftime('%Y-%m', data_hora_inicio) = ?
          AND status != 'CANCELADA'
        """;

        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, mes);
            try (ResultSet r = comando.executeQuery()) {
                if (r.next()) return r.getDouble("total");
            }
        }
        return 0.0;
    }

    public List<Visita> listarDoMes(String mes) throws SQLException { // MÉTODO: visitas não-canceladas do mês, com dados de comissão
        String sql = """
        SELECT v.*, f.nome AS nome_guia, r.nome AS nome_recepcionista
        FROM visita v
        JOIN funcionario f ON v.guia_id = f.id
        LEFT JOIN funcionario r ON v.recepcionista_id = r.id
        WHERE strftime('%Y-%m', v.data_hora_inicio) = ?
          AND v.status != 'CANCELADA'
        """;

        List<Visita> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, mes);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    Visita visita = new Visita();
                    visita.setId(resultado.getInt("id"));
                    visita.setNomeGuia(resultado.getString("nome_guia"));
                    visita.setNomeRecepcionista(resultado.getString("nome_recepcionista"));
                    visita.setQuantidadeInteira(resultado.getInt("quantidade_inteira"));
                    visita.setQuantidadeMeia(resultado.getInt("quantidade_meia"));
                    visita.setValorTotal(resultado.getDouble("valor_total"));
                    visita.setValorReembolsado(resultado.getDouble("valor_reembolsado"));
                    visita.setGuiaTipoRemuneracao(resultado.getString("guia_tipo_remuneracao"));
                    visita.setGuiaValorRemuneracao(resultado.getDouble("guia_valor_remuneracao"));
                    visita.setRecepcionistaTipoRemuneracao(resultado.getString("recepcionista_tipo_remuneracao"));
                    Object recepValor = resultado.getObject("recepcionista_valor_remuneracao");
                    visita.setRecepcionistaValorRemuneracao(recepValor == null ? null : ((Number) recepValor).doubleValue());
                    lista.add(visita);
                }
            }
        }
        return lista;
    }

    public List<Visita> buscar(Integer guiaId, String data) throws SQLException { // MÉTODO: busca visitas por guia e/ou data (ambos opcionais)
        StringBuilder sql = new StringBuilder("""
        SELECT v.*, f.nome AS nome_guia, r.nome AS nome_recepcionista
        FROM visita v
        JOIN funcionario f ON v.guia_id = f.id
        LEFT JOIN funcionario r ON v.recepcionista_id = r.id
        WHERE 1 = 1
        """);

        if (guiaId != null) {
            sql.append(" AND v.guia_id = ? ");
        }
        if (data != null && !data.isBlank()) {
            sql.append(" AND date(v.data_hora_inicio) = ? ");
        }
        sql.append(" ORDER BY v.data_hora_inicio DESC ");

        List<Visita> lista = new ArrayList<>();
        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql.toString())) {
            int indice = 1;
            if (guiaId != null) {
                comando.setInt(indice++, guiaId);
            }
            if (data != null && !data.isBlank()) {
                comando.setString(indice++, data);
            }

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    Visita visita = new Visita();
                    visita.setId(resultado.getInt("id"));
                    visita.setNomeGuia(resultado.getString("nome_guia"));
                    visita.setNomeRecepcionista(resultado.getString("nome_recepcionista"));
                    visita.setQuantidadeInteira(resultado.getInt("quantidade_inteira"));
                    visita.setQuantidadeMeia(resultado.getInt("quantidade_meia"));
                    visita.setQuantidadeNaoPagante(resultado.getInt("quantidade_nao_pagante"));
                    visita.setValorTotal(resultado.getDouble("valor_total"));
                    visita.setValorReembolsado(resultado.getDouble("valor_reembolsado"));
                    visita.setStatus(resultado.getString("status"));
                    visita.setDataHoraInicio(resultado.getString("data_hora_inicio"));
                    lista.add(visita);
                }
            }
        }

        return lista;
    }
}