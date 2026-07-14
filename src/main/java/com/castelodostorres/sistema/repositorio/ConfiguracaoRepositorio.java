package com.castelodostorres.sistema.repositorio;

import com.castelodostorres.sistema.banco.GerenciadorConexao;
import com.castelodostorres.sistema.modelo.Configuracao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfiguracaoRepositorio {

    public Configuracao buscar() throws SQLException {
        String sql = "SELECT * FROM configuracao WHERE id = 1";

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            if (resultado.next()) {
                Configuracao config = new Configuracao();
                config.setIpCatraca(resultado.getString("ip_catraca"));
                config.setPortaCatraca(resultado.getInt("porta_catraca"));
                config.setUsuarioCatraca(resultado.getString("usuario_catraca"));
                config.setSenhaCatraca(resultado.getString("senha_catraca"));
                config.setValorInteira(resultado.getDouble("valor_inteira"));
                config.setValorMeia(resultado.getDouble("valor_meia"));
                return config;
            }
        }

        return null;
    }

    public void salvar(Configuracao configuracao) throws SQLException {
        String sql = """
        INSERT INTO configuracao (id, ip_catraca, porta_catraca, usuario_catraca, senha_catraca, valor_inteira, valor_meia)
        VALUES (1, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
            ip_catraca = excluded.ip_catraca,
            porta_catraca = excluded.porta_catraca,
            usuario_catraca = excluded.usuario_catraca,
            senha_catraca = excluded.senha_catraca,
            valor_inteira = excluded.valor_inteira,
            valor_meia = excluded.valor_meia
        """;

        Connection conexao = GerenciadorConexao.getConexao();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, configuracao.getIpCatraca());
            comando.setInt(2, configuracao.getPortaCatraca());
            comando.setString(3, configuracao.getUsuarioCatraca());
            comando.setString(4, configuracao.getSenhaCatraca());
            comando.setDouble(5, configuracao.getValorInteira());
            comando.setDouble(6, configuracao.getValorMeia());
            comando.executeUpdate();
        }
    }
}