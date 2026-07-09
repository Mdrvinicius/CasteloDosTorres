package com.castelodostorres.sistema.modelo;

public class ConfiguracaoConexao {

    private final String ip;
    private final int porta;
    private final String usuario;
    private final String senha;

    public ConfiguracaoConexao(String ip, int porta, String usuario, String senha) {
        this.ip = ip;
        this.porta = porta;
        this.usuario = usuario;
        this.senha = senha;
    }

    public String getIp() {
        return ip;
    }

    public int getPorta() {
        return porta;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenha() {
        return senha;
    }
}