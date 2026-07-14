package com.castelodostorres.sistema.modelo;

public class Configuracao {
    private String ipCatraca;
    private int portaCatraca;
    private String usuarioCatraca;
    private String senhaCatraca;
    private double valorInteira;
    private double valorMeia;

    public String getIpCatraca() { return ipCatraca; }
    public void setIpCatraca(String ipCatraca) { this.ipCatraca = ipCatraca; }
    public int getPortaCatraca() { return portaCatraca; }
    public void setPortaCatraca(int portaCatraca) { this.portaCatraca = portaCatraca; }
    public String getUsuarioCatraca() { return usuarioCatraca; }
    public void setUsuarioCatraca(String usuarioCatraca) { this.usuarioCatraca = usuarioCatraca; }
    public String getSenhaCatraca() { return senhaCatraca; }
    public void setSenhaCatraca(String senhaCatraca) { this.senhaCatraca = senhaCatraca; }
    public double getValorInteira() { return valorInteira; }
    public void setValorInteira(double valorInteira) { this.valorInteira = valorInteira; }
    public double getValorMeia() { return valorMeia; }
    public void setValorMeia(double valorMeia) { this.valorMeia = valorMeia; }
}